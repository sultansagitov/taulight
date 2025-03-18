package net.result.taulight.link;

import net.result.sandnode.exception.InvalidSandnodeLinkException;
import net.result.sandnode.util.Endpoint;
import net.result.taulight.db.InviteCodeObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URISyntaxException;

public record ChannelLinkRecord(Endpoint endpoint, String code) {
    public ChannelLinkRecord(@NotNull Endpoint endpoint, @NotNull InviteCodeObject token) {
        this(endpoint, token.getCode());
    }

    @Contract("_ -> new")
    public static @NotNull ChannelLinkRecord fromString(@NotNull String s) throws InvalidSandnodeLinkException {
        URI uri;
        try {
            uri = new URI(s);
        } catch (URISyntaxException e) {
            throw new InvalidSandnodeLinkException(e);
        }

        Endpoint endpoint = new Endpoint(uri.getHost(), uri.getPort() == -1 ? 52525 : uri.getPort());

        if (!uri.getScheme().equals("sandnode")) {
            throw new InvalidSandnodeLinkException("Invalid scheme: " + uri.getScheme());
        }

        String nodeType = uri.getUserInfo();

        if (nodeType == null) throw new InvalidSandnodeLinkException("Node type cannot be null");

        String code = getCode(uri);

        return new ChannelLinkRecord(endpoint, code);
    }

    private static @NotNull String getCode(URI uri) throws InvalidSandnodeLinkException {
        String code = null;
        String query = uri.getQuery();
        if (query != null) {
            String[] queryParams = query.split("&");
            for (String param : queryParams) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    if (keyValue[0].equals("code")) {
                        code = keyValue[1];
                    }
                }
            }
        }

        if (code == null) {
            throw new InvalidSandnodeLinkException("Encryption type not found in query parameters");
        }
        return code;
    }

    @Override
    public String toString() {
        return "sandnode://channel@%s?code=%s".formatted(endpoint, code);
    }
}