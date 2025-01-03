package net.result.sandnode.messages.util;

import net.result.sandnode.exceptions.HeadersSerializationException;
import net.result.sandnode.exceptions.NoSuchMessageTypeException;
import net.result.sandnode.exceptions.NoSuchEncryptionException;
import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.interfaces.IEncryption;
import net.result.simplesix64.SimpleSix64;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

import static net.result.sandnode.encryption.Encryption.NONE;
import static net.result.sandnode.messages.util.NodeType.HUB;

public class Headers {
    private final Map<String, String> map = new HashMap<>();
    private short chainID = -1;
    private boolean fin = false;
    private @Nullable Connection connection;
    private @Nullable MessageType type;
    private IEncryption bodyEncryption = NONE;

    public Headers setFin(boolean fin) {
        this.fin = fin;
        return this;
    }

    public boolean getFin() {
        return fin;
    }

    public Headers setValue(@NotNull String key, @NotNull String value) {
        map.put(key.toLowerCase(), value.toLowerCase());
        return this;
    }

    public int getCount() {
        return map.size();
    }

    public Optional<String> getOptionalValue(String key) {
        return Optional.ofNullable(map.get(key.toLowerCase()));
    }

    public @NotNull String getValue(@NotNull String key) throws IllegalArgumentException {
        if (!map.containsKey(key.toLowerCase())) {
            throw new IllegalArgumentException("headers don't have key \"%s\"".formatted(key));
        }
        return map.get(key.toLowerCase());
    }

    public Headers setType(@NotNull MessageType type) {
        this.type = type;
        return this;
    }

    public @NotNull MessageType getType() throws NullPointerException {
        return Objects.requireNonNull(type);
    }

    public Headers setConnection(@NotNull Connection connection) {
        this.connection = connection;
        return this;
    }

    public @NotNull Connection getConnection() throws NullPointerException {
        return Objects.requireNonNull(connection);
    }

    public Headers setBodyEncryption(@NotNull IEncryption bodyEncryption) {
        this.bodyEncryption = bodyEncryption;
        return this;
    }

    public @NotNull IEncryption getBodyEncryption() throws NullPointerException {
        return Objects.requireNonNull(bodyEncryption);
    }

    public Headers setChainID(short chainID) {
        this.chainID = chainID;
        return this;
    }

    public short getChainID() {
        return chainID;
    }

    public static Headers getFromBytes(byte @NotNull [] data) throws NoSuchEncryptionException,
            NoSuchMessageTypeException {
        if (data.length < 5) {
            throw new IllegalArgumentException("Data is too short to extract the required information");
        }

        Headers headers = new Headers()
                .setConnection(Connection.fromByte(data[0]))
                .setType(MessageTypeManager.getMessageType(data[1]))
                .setBodyEncryption(EncryptionManager.find(data[2]))
                .setFin((data[0] & 0b00100000) != 0)
                .setChainID((short) (((data[3] & 0xFF) << 8) | (data[4] & 0xFF)));


        // boolean flag1 = (data[0] & 0b00010000) != 0;
        // boolean flag2 = (data[0] & 0b00001000) != 0;
        // boolean flag3 = (data[0] & 0b00000100) != 0;
        // boolean flag4 = (data[0] & 0b00000010) != 0;
        // boolean flag5 = (data[0] & 0b00000001) != 0;

        byte[] encoded = Arrays.copyOfRange(data, 5, data.length);
        String headersString = SimpleSix64.decode(encoded);

        for (String string : headersString.split(";")) {
            if (string.contains(":")) {
                int colonIndex = string.indexOf(":");
                if (colonIndex > 0) {
                    String key = string.substring(0, colonIndex).trim();
                    String value = string.substring(colonIndex + 1).trim();
                    headers.setValue(key, value);
                }
            }

        }

        return headers;
    }

    public byte[] toByteArray() throws HeadersSerializationException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        byte first = (byte) new Random().nextInt(16);
        if (getConnection().getFrom() == HUB) first |= (byte) 0b10000000;
        if (getConnection().getTo() == HUB) first |= (byte) 0b01000000;
        if (getFin()) first |= (byte) 0b00100000;
        byteArrayOutputStream.write(first);
        byteArrayOutputStream.write(getType().asByte());
        byteArrayOutputStream.write(getBodyEncryption().asByte());
        byteArrayOutputStream.write((getChainID() >> 8) & 0xFF);
        byteArrayOutputStream.write(getChainID() & 0xFF);

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
            throw new HeadersSerializationException("Failed to serialize headers to byte array", e);
        }

        return byteArrayOutputStream.toByteArray();
    }

    public @NotNull Headers copy() {
        Headers copy = new Headers();
        copy.map.putAll(this.map);
        copy.chainID = this.chainID;
        copy.fin = this.fin;
        copy.connection = this.connection;
        copy.type = this.type;
        copy.bodyEncryption = this.bodyEncryption;
        return copy;
    }
}
