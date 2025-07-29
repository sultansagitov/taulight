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
     * Sends a message in a chat, attempting to encrypt the message if possible.
     * Falls back to plaintext if encryption keys are unavailable.
     *
     * @param chat  the chat metadata including type and participant info
     * @param input the message input DTO to populate with content
     * @param text  the raw message content to send
     * @return the UUID of the successfully sent message
     */
    public synchronized UUID messageWithFallback(ChatInfoDTO chat, ChatMessageInputDTO input, String text)
            throws InterruptedException, ProtocolException, SandnodeErrorException, CryptoException {
        switch (chat.chatType) {
            case DIALOG -> {
                try {
                    String otherNickname = chat.otherNickname;
                    KeyEntry dek = client.node().agent().config.loadDEK(client.address, otherNickname);

                    LOGGER.debug("Using {} {}", dek.id(), dek.keyStorage());

                    input.setEncryptedContent(dek.id(), dek.keyStorage(), text);
                } catch (KeyStorageNotFoundException e) {
                    LOGGER.error("Using null", e);
                    input.setContent(text);
                }
            }
            case GROUP -> input.setContent(text);
        }

        RawMessage uuidRaw = sendAndReceive(new ForwardRequest(input));
        uuidRaw.expect(MessageTypes.HAPPY);
        UUID uuid = new UUIDMessage(uuidRaw).uuid;
        receive().expect(MessageTypes.HAPPY);
        return uuid;
    }

    /**
     * Sends a message in a chat, requiring encryption for dialog chats.
     * This method does not fall back to plaintext if encryption keys are unavailable.
     *
     * @param chat  the chat metadata including type and participant info
     * @param input the message input DTO to populate with content
     * @param text  the raw message content to send
     * @return the UUID of the successfully sent message
     */
    @SuppressWarnings("unused")
    public synchronized UUID messageWithoutFallback(ChatInfoDTO chat, ChatMessageInputDTO input, String text)
            throws InterruptedException, ProtocolException, SandnodeErrorException, CryptoException {
        switch (chat.chatType) {
            case DIALOG -> {
                String otherNickname = chat.otherNickname;
                KeyEntry dek = client.node().agent().config.loadDEK(client.address, otherNickname);
                LOGGER.debug(".Using {} {}", dek.id(), dek.keyStorage());
                input.setEncryptedContent(dek.id(), dek.keyStorage(), text);
            }
            case GROUP -> input.setContent(text);
        }

        RawMessage uuidRaw = sendAndReceive(new ForwardRequest(input));
        uuidRaw.expect(MessageTypes.HAPPY);
        UUID uuid = new UUIDMessage(uuidRaw).uuid;
        receive().expect(MessageTypes.HAPPY);
        return uuid;
    }
}
