package net.result.sandnode.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class Endpoint {

    public final String host;
    public final int port;

    public Endpoint(@NotNull String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static @NotNull Endpoint getFromString(@NotNull String domainString, int defaultPort) {
        String[] split = domainString.split(":");
        String host = split[0];
        int port = (split.length > 1) ? Integer.parseInt(split[1]) : defaultPort;
        return new Endpoint(host, port);
    }

    @Contract(pure = true)
    @Override
    public @NotNull String toString() {
        return String.format("%s:%d", host, port);
    }
}
