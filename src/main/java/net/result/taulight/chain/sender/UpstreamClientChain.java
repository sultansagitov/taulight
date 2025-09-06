package net.result.taulight.chain.sender;

import net.result.sandnode.chain.Chain;
import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.config.KeyEntry;
import net.result.sandnode.exception.error.KeyStorageNotFoundException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.taulight.dto.ChatInfoDTO;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.dto.UpstreamResponseDTO;
import net.result.taulight.message.types.UpstreamRequest;
import net.result.taulight.message.types.UpstreamResponse;
import net.result.taulight.util.TauAgentProtocol;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

/**
 * A client-side chain for sending message requests using the Sandnode protocol.
 * This class supports sending encrypted or plaintext messages based on the availability of encryption keys.
 */
public class UpstreamClientChain extends ClientChain {
    private static final Logger LOGGER = LogManager.getLogger(UpstreamClientChain.class);

    public static @NotNull UpstreamClientChain getNamed(SandnodeClient client, UUID id) {
        String chainName = "upstream-%s".formatted(id);
        Optional<Chain> opt = client.io().chainManager.getChain(chainName);

        UpstreamClientChain chain;
        if (opt.isPresent()) {
            chain = (UpstreamClientChain) opt.get();
        } else {
            chain = new UpstreamClientChain(client);
            client.io().chainManager.linkChain(chain);
            chain.chainName(chainName);
        }
        return chain;
    }

    public UpstreamClientChain(SandnodeClient client) {
        super(client);
    }

    /**
     * Sends a message in a chat.
     * If fallback is enabled and encryption keys are not available, sends plaintext.
     * If fallback is disabled and encryption keys are not available, throws an exception.
     * Optionally waits for delivery acknowledgment.
     *
     * @param chat               the chat metadata including type and participant info
     * @param input              the message input DTO to populate with content
     * @param text               the raw message content to send
     * @param fallback           whether to fallback to plaintext if encryption is not possible
     * @param requireDeliveryAck whether to wait for delivery acknowledgment (HAPPY message)
     * @return the UUID of the successfully sent message
     */
    public synchronized UpstreamResponseDTO sendMessage(ChatInfoDTO chat, ChatMessageInputDTO input, String text,
                                         boolean fallback, boolean requireDeliveryAck) {
        switch (chat.chatType) {
            case DIALOG -> {
                try {
                    String otherNickname = chat.otherNickname;
                    KeyEntry dek = TauAgentProtocol.loadDEK(client, otherNickname);
                    LOGGER.debug("Using {} {}", dek.id(), dek.keyStorage());
                    input.setEncryptedContent(dek.id(), dek.keyStorage(), text);
                } catch (KeyStorageNotFoundException e) {
                    if (fallback) {
                        LOGGER.error("Using null", e);
                        input.setContent(text);
                    } else {
                        throw e;
                    }
                }
            }
            case GROUP -> input.setContent(text);
        }

        final RawMessage uuidRaw = sendAndReceive(new UpstreamRequest(input, requireDeliveryAck));
        final UpstreamResponseDTO dto = new UpstreamResponse(uuidRaw).dto();

        if (requireDeliveryAck) {
            receive().expect(MessageTypes.HAPPY);
        }

        return dto;
    }
}