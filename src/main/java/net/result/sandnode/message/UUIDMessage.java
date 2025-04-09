package net.result.sandnode.message;

import net.result.sandnode.db.SandnodeEntity;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.message.util.Headers;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.UUID;

public class UUIDMessage extends Message {
    public final UUID uuid;

    public UUIDMessage(@NotNull Headers headers, UUID uuid) {
        super(headers);
        this.uuid = uuid;
    }

    public UUIDMessage(Headers headers, @NotNull SandnodeEntity object) {
        this(headers, object.id());
    }

    public UUIDMessage(@NotNull RawMessage raw) throws DeserializationException {
        super(raw.headers());
        byte[] body = raw.getBody();
        if (body.length != 16) {
            throw new DeserializationException("Invalid UUID byte array length: " + body.length);
        }
        ByteBuffer buffer = ByteBuffer.wrap(body);
        uuid = new UUID(buffer.getLong(), buffer.getLong());
    }

    @Override
    public byte[] getBody() {
        return ByteBuffer.allocate(16)
                .putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits())
                .array();
    }
}
