package net.result.sandnode.util;

import net.result.sandnode.exception.InvalidAddressSyntax;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URISyntaxException;

public record Address(String host, int port) {

    public static @NotNull Address getFromString(String input, int defaultPort) throws InvalidAddressSyntax {
        URI uri;
        try {
            uri = new URI("dummy://%s".formatted(input));
        } catch (URISyntaxException e) {
            throw new InvalidAddressSyntax(e);
        }
        String host = uri.getHost();
        int port = uri.getPort();

        if (host == null) {
            try {
                uri = new URI("dummy://[%s]".formatted(input));
            } catch (URISyntaxException e) {
                throw new InvalidAddressSyntax(e);
            }
            host = uri.getHost();
            port = defaultPort;

            if (host == null) {
                throw new InvalidAddressSyntax(input, "Unknown error");
            }
        }

        if (port > 65535) {
            throw new InvalidAddressSyntax(input, "Too big port");
        }

        return new Address(host, port != -1 ? port : defaultPort);
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
