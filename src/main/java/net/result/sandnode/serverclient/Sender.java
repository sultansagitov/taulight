package net.result.sandnode.serverclient;

import net.result.sandnode.encryption.Encryptions;
import net.result.sandnode.error.Errors;
import net.result.sandnode.error.SandnodeError;
import net.result.sandnode.exception.IllegalMessageLengthException;
import net.result.sandnode.exception.MessageSerializationException;
import net.result.sandnode.exception.MessageWriteException;
import net.result.sandnode.exception.crypto.CryptoException;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Sender {
    private static final Logger LOGGER = LogManager.getLogger(Sender.class);

    // Custom thread factory to name encryption threads
    private static final ThreadFactory encryptionThreadFactory = new ThreadFactory() {
        private final AtomicInteger count = new AtomicInteger(1);

        @Override
        public Thread newThread(@NotNull Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("Encryption-Worker-" + count.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        }
    };

    private static final ExecutorService encryptionPool = Executors.newFixedThreadPool(10, encryptionThreadFactory);

    // Helper class to store encrypted message with its sequence
    private static class EncryptionResult {
        final long sequence;
        final byte[] encryptedBytes;
        final Message originalMessage;

        public EncryptionResult(long sequence, byte[] encryptedBytes, Message message) {
            this.sequence = sequence;
            this.encryptedBytes = encryptedBytes;
            this.originalMessage = message;
        }
    }

    public static void sendingLoop(IOController io)
            throws InterruptedException, CryptoException, MessageWriteException {
        AtomicLong sequenceGenerator = new AtomicLong(0);
        ConcurrentSkipListMap<Long, EncryptionResult> resultsMap = new ConcurrentSkipListMap<>();
        CompletionService<EncryptionResult> completionService = new ExecutorCompletionService<>(encryptionPool);

        // Dispatcher thread to fetch messages and submit encryption tasks
        Thread dispatcher = new Thread(() -> {
            try {
                while (io.connected) {
                    Message message = io.sendingQueue.take();
                    beforeSending(io, message);
                    long sequence = sequenceGenerator.getAndIncrement();

                    completionService.submit(() -> {
                        try {
                            byte[] byteArray = MessageUtil.encryptMessage(message, io.keyStorageRegistry).toByteArray();
                            return new EncryptionResult(sequence, byteArray, message);
                        } catch (Exception e) {
                            LOGGER.error("Encryption failed", e);
                            ErrorMessage errorMessage = new ErrorMessage(getErrorForException(e));
                            errorMessage.setHeadersEncryption(message.headersEncryption());
                            errorMessage.headers()
                                    .setBodyEncryption(message.headers().bodyEncryption())
                                    .setChainID(message.headers().chainID())
                                    .setConnection(message.headers().connection());
                            byte[] byteArray = MessageUtil.encryptMessage(errorMessage, io.keyStorageRegistry).toByteArray();
                            return new EncryptionResult(sequence, byteArray, errorMessage);
                        }
                    });
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        dispatcher.setName("Encryption-Dispatcher");
        dispatcher.start();

        long nextSequenceToSend = 0;

        while (io.connected) {
            try {
                Future<EncryptionResult> future = completionService.take(); // blocks until result is ready
                EncryptionResult result = future.get(); // handles exceptions below
                resultsMap.put(result.sequence, result);

                // Send in sequence
                while (resultsMap.containsKey(nextSequenceToSend)) {
                    EncryptionResult toSend = resultsMap.remove(nextSequenceToSend);
                    io.out.write(toSend.encryptedBytes);
                    io.out.flush();
                    LOGGER.info("Message sent: {}", toSend.originalMessage);
                    nextSequenceToSend++;
                }

            } catch (ExecutionException e) {
                LOGGER.error("Failed to encrypt message: {}", e.getCause().getMessage(), e.getCause());
            } catch (IOException e) {
                throw new MessageWriteException(null, "Failed to write encrypted message", e);
            }
        }

        encryptionPool.shutdownNow();
        dispatcher.join();
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
