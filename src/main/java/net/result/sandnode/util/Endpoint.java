package net.result.sandnode.util;

import net.result.sandnode.exception.InvalidEndpointSyntax;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URISyntaxException;

public record Endpoint(String host, int port) {

    public static @NotNull Endpoint getFromString(String input, int defaultPort) throws InvalidEndpointSyntax {
        URI uri;
        try {
            uri = new URI("dummy://%s".formatted(input));
        } catch (URISyntaxException e) {
            throw new InvalidEndpointSyntax(e);
        }
        String host = uri.getHost();
        int port = uri.getPort();

        if (host == null) {
            try {
                uri = new URI("dummy://[%s]".formatted(input));
            } catch (URISyntaxException e) {
                throw new InvalidEndpointSyntax(e);
            }
            host = uri.getHost();
            port = defaultPort;

            if (host == null) {
                throw new InvalidEndpointSyntax(input, "Unknown error");
            }
        }

        if (port > 65535) {
            throw new InvalidEndpointSyntax(input, "Too big port");
        }

        return new Endpoint(host, port != -1 ? port : defaultPort);
    }

    @Contract(pure = true)
    @Override
    public @NotNull String toString() {
        return "%s:%d".formatted(host, port);
    }

    public @NotNull String toString(int defaultPort) {
        return port == defaultPort ? host : toString();
    }
}
