package net.result.sandnode.messages;

import net.result.sandnode.messages.util.Connection;
import net.result.sandnode.messages.util.MessageType;
import net.result.sandnode.util.encryption.Encryption;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import static net.result.sandnode.messages.util.Connection.USER2HUB;
import static net.result.sandnode.messages.util.MessageType.MSG;

public class HeadersBuilder {
    private static final Logger LOGGER = LogManager.getLogger(HeadersBuilder.class);
    private final Map<String, String> map = new HashMap<>();
    private @Nullable Connection connection;
    private @Nullable MessageType type;
    private @Nullable String contentType;
    private @Nullable Encryption encryption;

    public HeadersBuilder set(@NotNull Connection connection) {
        this.connection = connection;
        return this;
    }

    public HeadersBuilder set(@NotNull MessageType type) {
        this.type = type;
        return this;
    }

    public HeadersBuilder set(@NotNull Encryption encryption) {
        this.encryption = encryption;
        return this;
    }

    public HeadersBuilder set(@NotNull String contentType) {
        this.contentType = contentType;
        return this;
    }

    public HeadersBuilder set(@NotNull String key, @NotNull String value) {
        if (key.equalsIgnoreCase("ct")) set(value);
        else map.put(key, value);
        return this;
    }


    public Headers build() {
        Headers headers = new Headers(
                (connection != null) ? connection : USER2HUB,
                (type != null) ? type : MSG,
                (contentType != null) ? contentType : "application/json"
        );

        if (connection == null) LOGGER.warn("Connection is null; defaulting to USER2HUB.");
        if (type == null) LOGGER.warn("Type is null; defaulting to MSG.");
        if (contentType == null) LOGGER.warn("ContentType is null; defaulting to application/json.");

        headers.setEncryption((encryption != null) ? encryption : Encryption.NO);
        for (Map.Entry<String, String> entry : map.entrySet())
            headers.set(entry.getKey(), entry.getValue());
        return headers;
    }
}
