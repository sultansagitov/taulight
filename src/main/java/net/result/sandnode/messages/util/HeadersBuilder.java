package net.result.sandnode.messages.util;

import net.result.sandnode.util.encryption.interfaces.IEncryption;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HeadersBuilder {
    private static final Logger LOGGER = LogManager.getLogger(HeadersBuilder.class);
    private final Map<String, String> map = new HashMap<>();
    private @Nullable Connection connection;
    private @Nullable IMessageType type;
    private @Nullable IEncryption encryption;

    public HeadersBuilder set(@NotNull Connection connection) {
        this.connection = connection;
        return this;
    }

    public HeadersBuilder set(@NotNull IMessageType type) {
        this.type = type;
        return this;
    }

    public HeadersBuilder set(@NotNull IEncryption encryption) {
        this.encryption = encryption;
        return this;
    }

    public HeadersBuilder set(@NotNull String key, @NotNull String value) {
        map.put(key, value);
        return this;
    }

    public Headers build() {
        Headers headers;
        try {
            headers = new Headers(
                    Objects.requireNonNull(connection),
                    Objects.requireNonNull(type)
            );

            headers.setBodyEncryption(Objects.requireNonNull(encryption));
        } catch (NullPointerException e) {
            LOGGER.error("Headers building error", e);
            throw e;
        }
        for (Map.Entry<String, String> entry : map.entrySet()) {
            headers.set(entry.getKey(), entry.getValue());
        }
        return headers;
    }
}
