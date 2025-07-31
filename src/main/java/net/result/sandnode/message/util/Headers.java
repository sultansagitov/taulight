package net.result.sandnode.message.util;

import net.result.sandnode.encryption.Encryptions;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.HeadersSerializationException;
import net.result.sandnode.exception.NoSuchMessageTypeException;
import net.result.sandnode.exception.crypto.NoSuchEncryptionException;
import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.interfaces.Encryption;
import net.result.simplesix64.SimpleSix64;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class Headers {
    private final Map<String, String> map = new HashMap<>();
    private short chainID;
    private boolean fin = false;
    private Connection connection = Connection.AGENT2HUB;
    private MessageType type = MessageTypes.HAPPY;
    private Encryption bodyEncryption = Encryptions.NONE;

    public Headers setFin(boolean fin) {
        this.fin = fin;
        return this;
    }

    public boolean fin() {
        return fin;
    }

    public int count() {
        return map.size();
    }

    public boolean has(String key) {
        return map.containsKey(key.toLowerCase());
    }

    public Headers setValue(@NotNull String key, @NotNull String value) {
        map.put(key.toLowerCase(), value.toLowerCase());
        return this;
    }

    public UUID getUUID(String key) throws DeserializationException {
        if (!has(key)) throw new DeserializationException("headers don't have key \"%s\"".formatted(key));
        try {
            return UUID.fromString(map.get(key.toLowerCase()));
        } catch (Exception e) {
            throw new DeserializationException(e);
        }
    }

    public Optional<String> getOptionalValue(String key) {
        return Optional.ofNullable(map.get(key.toLowerCase()));
    }

    public @NotNull String getValue(@NotNull String key) throws DeserializationException {
        if (!has(key)) {
            throw new DeserializationException("headers don't have key \"%s\"".formatted(key));
        }
        return map.get(key.toLowerCase());
    }

    public @Nullable String getValueNullable(@NotNull String key) {
        return has(key) ? map.get(key.toLowerCase()) : null;
    }

    public Headers setType(@NotNull MessageType type) {
        this.type = type;
        return this;
    }

    public @NotNull MessageType type() throws NullPointerException {
        return type;
    }

    public Headers setConnection(@NotNull Connection connection) {
        this.connection = connection;
        return this;
    }

    public @NotNull Connection connection() throws NullPointerException {
        return connection;
    }

    public Headers setBodyEncryption(@NotNull Encryption bodyEncryption) {
        this.bodyEncryption = bodyEncryption;
        return this;
    }

    public @NotNull Encryption bodyEncryption() throws NullPointerException {
        return bodyEncryption;
    }

    public Headers setChainID(short chainID) {
        this.chainID = chainID;
        return this;
    }

    public short chainID() {
        return chainID;
    }

    @SuppressWarnings("CommentedOutCode")
    public static Headers fromBytes(byte @NotNull [] data)
            throws NoSuchEncryptionException, NoSuchMessageTypeException {
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
        if (connection().getFrom() == NodeType.HUB) first |= (byte) 0b10000000;
        if (connection().getTo() == NodeType.HUB) first |= (byte) 0b01000000;
        if (fin()) first |= (byte) 0b00100000;
        byteArrayOutputStream.write(first);
        byteArrayOutputStream.write(type().asByte());
        byteArrayOutputStream.write(bodyEncryption().asByte());
        byteArrayOutputStream.write((chainID() >> 8) & 0xFF);
        byteArrayOutputStream.write(chainID() & 0xFF);

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
        Headers copy = new Headers()
                .setChainID(chainID())
                .setFin(fin())
                .setConnection(connection())
                .setType(type())
                .setBodyEncryption(bodyEncryption());
        copy.map.putAll(map);
        return copy;
    }

    public Set<String> keys() {
        return map.keySet();
    }
}
