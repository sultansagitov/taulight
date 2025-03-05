package net.result.sandnode.util;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.UUID;

public class UUIDUtil {
    public static byte @NotNull [] uuidToBinary(@NotNull UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    public static @NotNull UUID binaryToUUID(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long firstLong = bb.getLong();
        long secondLong = bb.getLong();
        return new UUID(firstLong, secondLong);
    }
}
