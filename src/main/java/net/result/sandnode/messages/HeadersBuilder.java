package net.result.sandnode.messages;

import net.result.sandnode.messages.util.Connection;
import net.result.sandnode.messages.util.MessageType;
import net.result.sandnode.util.encryption.Encryption;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import static net.result.sandnode.messages.util.Connection.CLIENT2SERVER;
import static net.result.sandnode.messages.util.MessageType.MESSAGE;

public class HeadersBuilder {
    private @Nullable Connection connection;
    private @Nullable MessageType type;
    private @Nullable String contentType;
    private @Nullable Encryption encryption;
    private final Map<String, String> map = new HashMap<>();

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
        final Headers headers = new Headers(
                (connection != null) ? connection : CLIENT2SERVER,
                (type != null) ? type : MESSAGE,
                (contentType != null) ? contentType : "application/json"
        );
        headers.encryption = (encryption != null) ? encryption : Encryption.NO;
        for (Map.Entry<String, String> entry : map.entrySet())
            headers.set(entry.getKey(), entry.getValue());
        return headers;
    }
}
