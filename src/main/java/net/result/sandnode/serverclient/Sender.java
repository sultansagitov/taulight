package net.result.sandnode.serverclient;

import net.result.sandnode.encryption.Encryptions;
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
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.util.IOController;
import net.result.sandnode.util.MessageUtil;
import net.result.sandnode.util.RandomUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class Sender {
    private static final Logger LOGGER = LogManager.getLogger(Sender.class);
    private static final int POOL_SIZE = 10;

    private record EncryptionResult(long sequence, byte[] encryptedBytes, Message originalMessage) { }

    public static void sendingLoop(@NotNull IOController io) throws InterruptedException, MessageWriteException {
        try (var executorResource = new ExecutorServiceResource<>(io, "Encryptor", POOL_SIZE, EncryptionResult::sequence)) {
            var sequenceGenerator = new AtomicLong(0);
            var resultsMap = new ConcurrentSkipListMap<Long, EncryptionResult>();

            Thread producer = new Thread(() -> {
                try {
                    while (io.connected) {
                        Message message = io.sendingQueue.take();
                        long sequence = sequenceGenerator.getAndIncrement();

                        executorResource.submit(() -> {
                            beforeSending(io, message);
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
                EncryptionResult result = executorResource.queue.take();

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

    private static void beforeSending(IOController io, Message message) {
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
