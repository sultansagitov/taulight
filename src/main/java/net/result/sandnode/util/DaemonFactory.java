package net.result.sandnode.util;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;

public class DaemonFactory implements ThreadFactory {
    @Override
    public Thread newThread(@NotNull Runnable r) {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        return thread;
    }
}
