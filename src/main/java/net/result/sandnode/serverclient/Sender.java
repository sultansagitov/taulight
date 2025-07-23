package net.result.sandnode.serverclient;

import net.result.sandnode.encryption.KeyStorageRegistry;
import net.result.sandnode.error.Errors;
import net.result.sandnode.error.SandnodeError;
import net.result.sandnode.exception.IllegalMessageLengthException;
import net.result.sandnode.exception.MessageSerializationException;
import net.result.sandnode.exception.MessageWriteException;
import net.result.sandnode.exception.error.EncryptionException;
import net.result.sandnode.exception.error.KeyStorageNotFoundException;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.types.ErrorMessage;
import net.result.sandnode.util.IOController;
import net.result.sandnode.util.MessageUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

public class Sender {
    private static final Logger LOGGER = LogManager.getLogger(Sender.class);
    private static final int POOL_SIZE = 10;

    private record EncryptionResult(long sequence, byte[] encryptedBytes, Message originalMessage) { }

    public static void sendingLoop(Peer peer) throws InterruptedException, MessageWriteException {
        IOController io = peer.io();
        try (var exec = new ExecutorServiceResource<>(io, "Encryptor", POOL_SIZE, EncryptionResult::sequence)) {
            var generator = new AtomicLong(0);
            var resultsMap = new ConcurrentSkipListMap<Long, EncryptionResult>();

            Thread producer = new Thread(() -> {
                try {
                    while (io.connected) {
                        Message message = io.sendingQueue.take();
                        long sequence = generator.getAndIncrement();

                        exec.submit(() -> {
                            peer.node().beforeSending(peer, message);
                            KeyStorageRegistry ksr = io.keyStorageRegistry;
                            try {
                                byte[] bytes = MessageUtil.encryptMessage(message, ksr).toByteArray();
                                return new EncryptionResult(sequence, bytes, message);
                            } catch (Exception e) {
                                LOGGER.error("Encryption failed", e);
                                Message fallback = createErrorMessage(e, message);
                                byte[] fallbackBytes = MessageUtil.encryptMessage(fallback, ksr).toByteArray();
                                return new EncryptionResult(sequence, fallbackBytes, fallback);
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Encryption-Producer");

            producer.setDaemon(true);
            producer.start();

            long nextSequenceToSend = 0;

            while (io.connected) {
                EncryptionResult result = exec.queue.take();

                resultsMap.put(result.sequence, result);

                while (resultsMap.containsKey(nextSequenceToSend)) {
                    EncryptionResult toSend = resultsMap.remove(nextSequenceToSend);
                    try {
                        io.out.write(toSend.encryptedBytes);
                        io.out.flush();
                        LOGGER.info("Sent {}", toSend.originalMessage);
                        nextSequenceToSend++;
                    } catch (IOException e) {
                        throw new MessageWriteException("Failed to write encrypted message", e);
                    }
                }
            }

            producer.join();
        }
    }

    private static Message createErrorMessage(Exception e, Message original) {
        ErrorMessage errorMessage = new ErrorMessage(getErrorForException(e));
        errorMessage.setHeadersEncryption(original.headersEncryption());
        errorMessage.headers()
                .setBodyEncryption(original.headers().bodyEncryption())
                .setChainID(original.headers().chainID())
                .setConnection(original.headers().connection());
        return errorMessage;
    }

    private static SandnodeError getErrorForException(Exception e) {
        if (e instanceof MessageSerializationException || e instanceof IllegalMessageLengthException) {
            return Errors.SERVER;
        } else if (e instanceof KeyStorageNotFoundException) {
            return Errors.KEY_NOT_FOUND;
        } else if (e instanceof EncryptionException) {
            return Errors.ENCRYPT;
        }
        return Errors.SERVER;
    }
}
