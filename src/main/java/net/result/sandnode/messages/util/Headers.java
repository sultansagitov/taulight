package net.result.sandnode.messages.util;

import net.result.sandnode.exceptions.MessageSerializationException;
import net.result.sandnode.exceptions.NoSuchMessageTypeException;
import net.result.sandnode.exceptions.NoSuchEncryptionException;
import net.result.sandnode.encryption.Encryptions;
import net.result.sandnode.encryption.interfaces.IEncryption;
import net.result.simplesix64.SimpleSix64;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

import static net.result.sandnode.messages.util.NodeType.HUB;

public class Headers {
    private final Map<String, String> map = new HashMap<>();
    private @Nullable Connection connection;
    private @Nullable IMessageType type;
    private @Nullable IEncryption encryption;
    private boolean fin = false;

    public static Headers getFromBytes(byte @NotNull [] data) throws NoSuchEncryptionException, NoSuchMessageTypeException {
        if (data.length < 3) {
            throw new IllegalArgumentException("Data is too short to extract the required information");
        }

        Headers headers = new Headers()
                .set(Connection.fromByte(data[0]))
                .set(MessageTypes.getMessageType(data[1]))
                .set(Encryptions.find(data[2]))
                .setFin((data[0] & 0b00100000) != 0);

        // boolean flag1 = (data[0] & 0b00010000) != 0;
        // boolean flag2 = (data[0] & 0b00001000) != 0;
        // boolean flag3 = (data[0] & 0b00000100) != 0;
        // boolean flag4 = (data[0] & 0b00000010) != 0;
        // boolean flag5 = (data[0] & 0b00000001) != 0;

        byte[] encoded = Arrays.copyOfRange(data, 3, data.length);
        String headersString = SimpleSix64.decode(encoded);

        for (String string : headersString.split(";")) {
            if (string.contains(":")) {
                String[] keyValue = string.split(":");
                headers.set(keyValue[0], keyValue[1]);
            }
        }

        return headers;
    }

    public Headers setFin(boolean fin) {
        this.fin = fin;
        return this;
    }

    public Headers set(@NotNull Connection connection) {
        this.connection = connection;
        return this;
    }

    public Headers set(@NotNull IMessageType type) {
        this.type = type;
        return this;
    }

    public Headers set(@NotNull IEncryption encryption) {
        this.encryption = encryption;
        return this;
    }

    public Headers set(@NotNull String key, @NotNull String value) {
        map.put(key, value);
        return this;
    }

    public boolean getFin() {
        return fin;
    }

    public @NotNull IMessageType getType() throws NullPointerException {
        return Objects.requireNonNull(type);
    }

    public @NotNull IEncryption getBodyEncryption() throws NullPointerException {
        return Objects.requireNonNull(encryption);
    }

    public @NotNull Connection getConnection() throws NullPointerException {
        return Objects.requireNonNull(connection);
    }

    public @NotNull String get(@NotNull String key) throws IllegalArgumentException {
        if (!map.containsKey(key)) {
            throw new IllegalArgumentException(String.format("headers don't have key \"%s\"", key));
        }
        return map.get(key);
    }

    public byte[] toByteArray() throws MessageSerializationException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        byte first = (byte) new Random().nextInt(16);
        if (getConnection().getFrom() == HUB) first |= (byte) 0b10000000;
        if (getConnection().getTo() == HUB) first |= (byte) 0b01000000;
        if (getFin()) first |= (byte) 0b00100000;
        byteArrayOutputStream.write(first);
        byteArrayOutputStream.write(getType().asByte());
        byteArrayOutputStream.write(getBodyEncryption().asByte());

        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, String> entry : this.map.entrySet()) {
            result
                    .append(entry.getKey())
                    .append(":")
                    .append(entry.getValue())
                    .append(";");
        }

        byte[] encoded = SimpleSix64.encode(result.toString());
        String ignored = SimpleSix64.decode(encoded);

        try {
            byteArrayOutputStream.write(encoded);
        } catch (IOException e) {
            throw new MessageSerializationException("Failed to serialize message to byte array", e);
        }

        return byteArrayOutputStream.toByteArray();
    }

}
