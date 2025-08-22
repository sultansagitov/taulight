package net.result.sandnode.serverclient;

import net.result.sandnode.exception.SandnodeInterruptedException;
import net.result.sandnode.exception.UnexpectedSocketDisconnectException;
import net.result.sandnode.message.EncryptedMessage;
import net.result.sandnode.util.IOController;
import net.result.sandnode.util.MessageUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

public class Receiver {
    private static final int POOL_SIZE = 10;
    private static final Logger LOGGER = LogManager.getLogger(Receiver.class);

    public static void receivingLoop(IOController io, Peer peer) {
        AtomicLong sequenceCounter = new AtomicLong(0);
        AtomicLong nextExpected = new AtomicLong(0);

        try (var pool = new ExecutorServiceResource<>(io, "Decryptor", POOL_SIZE, SequencedRawMessage::sequence)) {
            ExecutorService decryptorPool = pool.executor();

            LOGGER.info("Receiving loop started");

            Thread readerThread = new Thread(() -> {
                try {
                    while (io.connected) {
                        EncryptedMessage encrypted = EncryptedMessage.readMessage(io.in);

                        long seq = sequenceCounter.getAndIncrement();

                        pool.submit(() -> {
                            var raw = MessageUtil.decryptMessage(encrypted, io.keyStorageRegistry);
                            return new SequencedRawMessage(seq, raw);
                        });
                    }
                } catch (UnexpectedSocketDisconnectException e) {
                    peer.close();
                } catch (Exception e) {
                    throw new RuntimeException("Reader thread encountered an error", e);
                }
            }, "%s/Receiver-Reader".formatted(io.addressFromSocket()));

            Thread distributorThread = new Thread(() -> {
                try {
                    while (io.connected || !decryptorPool.isTerminated() || !pool.queue.isEmpty()) {
                        SequencedRawMessage next = pool.queue.peek();
                        if (next != null && next.sequence() == nextExpected.get()) {
                            pool.queue.poll();
                            io.chainManager.distributeMessage(next.message());
                            nextExpected.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("Distributor thread encountered an error", e);
                }
            }, "%s/Receiver-Distributor".formatted(io.addressFromSocket()));
            distributorThread.setDaemon(true);
            readerThread.start();
            distributorThread.start();

            try {
                readerThread.join();
                distributorThread.join();
            } catch (InterruptedException e) {
                throw new SandnodeInterruptedException(e);
            }

            LOGGER.info("Receiving loop stopped");
        }
    }
}
