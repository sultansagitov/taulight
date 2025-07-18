package net.result.sandnode.serverclient;

import net.result.sandnode.encryption.Encryptions;
import net.result.sandnode.error.Errors;
import net.result.sandnode.error.SandnodeError;
import net.result.sandnode.exception.IllegalMessageLengthException;
import net.result.sandnode.exception.MessageSerializationException;
import net.result.sandnode.exception.MessageWriteException;
import net.result.sandnode.exception.SandnodeException;
import net.result.sandnode.exception.error.EncryptionException;
import net.result.sandnode.exception.error.KeyStorageNotFoundException;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.types.ErrorMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.util.IOController;
import net.result.sandnode.util.MessageUtil;
import net.result.sandnode.util.RandomUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class Sender {
    private static final Logger LOGGER = LogManager.getLogger(Sender.class);

    public static void beforeSending(IOController io, Message message) {
        Headers headers = message.headers();
        headers.setConnection(io.connection);
        if (message.headersEncryption() == Encryptions.NONE) {
            message.setHeadersEncryption(io.currentEncryption());
        }
        if (headers.bodyEncryption() == Encryptions.NONE) {
            headers.setBodyEncryption(io.currentEncryption());
        }
        headers.setValue("random", RandomUtil.getRandomString());
    }

    public static void sendingLoop(IOController io) throws InterruptedException, SandnodeException {
        while (io.connected) {
            Message message = io.sendingQueue.take();
            beforeSending(io, message);

            Message sent = null;
            byte[] byteArray = null;
            SandnodeError error = null;
            try {
                byteArray = MessageUtil.encryptMessage(message, io.keyStorageRegistry).toByteArray();
                sent = message;
            } catch (MessageSerializationException | IllegalMessageLengthException e) {
                LOGGER.error("Serialization or message length issue", e);
                error = Errors.SERVER;
            } catch (KeyStorageNotFoundException e) {
                LOGGER.error("Key storage not found", e);
                error = Errors.KEY_NOT_FOUND;
            } catch (EncryptionException e) {
                LOGGER.error("Encryption or key issue", e);
                error = Errors.ENCRYPT;
            }

            if (error != null) {
                ErrorMessage errorMessage = new ErrorMessage(error);
                Headers headers = errorMessage.headers();
                errorMessage
                        .setHeadersEncryption(message.headersEncryption());
                headers
                        .setBodyEncryption(message.headers().bodyEncryption())
                        .setChainID(message.headers().chainID())
                        .setConnection(message.headers().connection());
                byteArray = MessageUtil.encryptMessage(errorMessage, io.keyStorageRegistry).toByteArray();
                sent = errorMessage;
            }

            try {
                io.out.write(byteArray);
                io.out.flush();
            } catch (IOException e) {
                throw new MessageWriteException(message, "Failed to write message to output.", e);
            }

            LOGGER.info("Message sent: {}", sent);
        }
    }
}
