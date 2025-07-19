package net.result.sandnode.serverclient;

import net.result.sandnode.message.EncryptedMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.util.IOController;
import net.result.sandnode.util.MessageUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public class Receiver {
    private static final Logger LOGGER = LogManager.getLogger(Receiver.class);

    public static void receivingLoop(IOController io) throws InterruptedException {
        BlockingQueue<SequencedRawMessage> decryptedQueue;
        decryptedQueue = new PriorityBlockingQueue<>(100, Comparator.comparingLong(SequencedRawMessage::sequence));

        AtomicLong sequenceCounter = new AtomicLong(0);
        AtomicLong nextExpected = new AtomicLong(0);

        try (ExecutorServiceResource poolResource = new ExecutorServiceResource()) {
            ExecutorService decryptorPool = poolResource.executor();

            LOGGER.info("Receiving loop started");

            Thread readerThread = new Thread(() -> {
                try {
                    while (io.connected) {
                        EncryptedMessage encrypted = EncryptedMessage.readMessage(io.in);

                        long seq = sequenceCounter.getAndIncrement();

                        decryptorPool.submit(() -> {
                            try {
                                RawMessage raw = MessageUtil.decryptMessage(encrypted, io.keyStorageRegistry);
                                decryptedQueue.put(new SequencedRawMessage(seq, raw));
                            } catch (Exception e) {
                                LOGGER.warn("Failed to decrypt message seq={}", seq, e);
                            }
                        });
                    }
                } catch (Exception e) {
                    LOGGER.error("Reader thread encountered an error", e);
                }
            });
            readerThread.setName("Receiver-Reader");

            Thread distributorThread = new Thread(() -> {
                try {
                    while (io.connected || !decryptorPool.isTerminated() || !decryptedQueue.isEmpty()) {
                        SequencedRawMessage next = decryptedQueue.peek();
                        if (next != null && next.sequence() == nextExpected.get()) {
                            decryptedQueue.poll();
                            io.chainManager.distributeMessage(next.message());
                            nextExpected.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("Distributor thread encountered an error", e);
                }
            });
            distributorThread.setName("Receiver-Distributor");
            distributorThread.setDaemon(true);
            readerThread.start();
            distributorThread.start();

            readerThread.join();
            distributorThread.join();

            LOGGER.info("Receiving loop stopped");
        }
    }
}
