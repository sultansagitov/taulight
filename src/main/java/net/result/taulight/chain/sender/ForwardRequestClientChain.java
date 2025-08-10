package net.result.taulight.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.config.KeyEntry;
import net.result.sandnode.exception.ProtocolException;
import net.result.sandnode.exception.crypto.CryptoException;
import net.result.sandnode.exception.error.KeyStorageNotFoundException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.UUIDMessage;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.taulight.dto.ChatInfoDTO;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.message.types.ForwardRequest;
import net.result.taulight.util.TauAgentProtocol;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

/**
 * A client-side chain for sending forward message requests using the Sandnode protocol.
 * This class supports sending encrypted or plaintext messages based on the availability of encryption keys.
 */
public class ForwardRequestClientChain extends ClientChain {
    private static final Logger LOGGER = LogManager.getLogger(ForwardRequestClientChain.class);

    public ForwardRequestClientChain(SandnodeClient client) {
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
    public synchronized UUID sendMessage(ChatInfoDTO chat, ChatMessageInputDTO input, String text,
                                         boolean fallback, boolean requireDeliveryAck)
            throws InterruptedException, ProtocolException, SandnodeErrorException, CryptoException {
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

        RawMessage uuidRaw = sendAndReceive(new ForwardRequest(input, requireDeliveryAck));
        uuidRaw.expect(MessageTypes.HAPPY);
        UUID uuid = new UUIDMessage(uuidRaw).uuid;

        if (requireDeliveryAck) {
            receive().expect(MessageTypes.HAPPY);
        }

        return uuid;
    }
}