package net.result.sandnode.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URISyntaxException;

public class Endpoint {

    public final String host;
    public final int port;

    public Endpoint(@NotNull String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static @NotNull Endpoint getFromString(@NotNull String input, int defaultPort) throws URISyntaxException {
        URI uri = new URI(String.format("dummy://%s", input));
        String host = uri.getHost();
        int port = uri.getPort();

        if (host == null) {
            uri = new URI(String.format("dummy://[%s]", input));
            host = uri.getHost();
            port = defaultPort;

            if (host == null) {
                throw new URISyntaxException(input, "Unknown error");
            }
        }

        if (port > 65535) {
            throw new URISyntaxException(input, "Too big port");
        }

        return new Endpoint(host, port != -1 ? port : defaultPort);
    }

    @Contract(pure = true)
    @Override
    public @NotNull String toString() {
        return String.format("%s:%d", host, port);
    }

    public @NotNull String toString(int defaultPort) {
        return port == defaultPort ? host : toString();
    }
}
