package net.result.sandnode.serverclient;

import net.result.sandnode.util.IOController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.ToLongFunction;

public class ExecutorServiceResource<T> implements AutoCloseable {
    private static final Logger LOGGER = LogManager.getLogger(ExecutorServiceResource.class);
    public final BlockingQueue<T> queue;
    private final ExecutorService executor;

    public ExecutorServiceResource(IOController io, final String thrName, int nThreads, ToLongFunction<T> sequence) {
        queue = new PriorityBlockingQueue<>(10, Comparator.comparingLong(sequence));

        ThreadFactory factory = new ThreadFactory() {
            private final AtomicInteger count = new AtomicInteger(1);

            @Override
            public Thread newThread(@NotNull Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("%s/%s-Worker-%d".formatted(io.addressFromSocket(), thrName, count.getAndIncrement()));
                thread.setDaemon(true);
                return thread;
            }
        };
        executor = Executors.newFixedThreadPool(nThreads, factory);
    }

    public ExecutorService executor() {
        return executor;
    }

    @Override
    public void close() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                    System.err.println("Executor did not terminate");
                }
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public void submit(Callable<T> callable) {
        executor.submit(() -> {
            try {
                T t = callable.call();
                queue.add(t);
            } catch (Exception e) {
                LOGGER.error("Result not added to queue", e);
            }
        });
    }
}
