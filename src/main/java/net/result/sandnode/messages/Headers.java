package net.result.sandnode.messages;

import net.result.sandnode.exceptions.NoSuchReqHandler;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.messages.util.Connection;
import net.result.sandnode.messages.util.MessageType;
import net.result.sandnode.messages.util.NodeType;
import net.result.sandnode.protocol.FromByte;
import net.result.sandnode.util.encryption.Encryption;
import net.result.simplesix64.SimpleSix64;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

import static net.result.sandnode.messages.util.NodeType.SERVER;

public class Headers implements Iterable<Map.Entry<String, String>>, IParameters {
    private final Map<String, String> map = new HashMap<>();
    private final Connection connection;
    private MessageType type;
    private Encryption encryption = Encryption.NO;

    public Headers(@NotNull Connection connection, @NotNull MessageType type, @NotNull String contentType) {
        this.connection = connection;
        this.type = type;
        this.setContentType(contentType);
    }

    @Override
    public Connection getConnection() {
        return connection;
    }


    public String get(@NotNull String headerName) {
        return map.get(headerName.trim().toLowerCase());
    }

    public void set(@NotNull String headerName, @NotNull String value) {
        map.put(headerName.trim().toLowerCase(), value.trim());
    }

    public void add(@NotNull Headers value) {
        map.putAll(value.map);
    }


    public boolean containsHeader(@NotNull String headerName) {
        return map.containsKey(headerName.toLowerCase());
    }

    @Override
    public @NotNull String getContentType() {
        String s = get("ct");
        return Optional.of(s).orElse("text/plain").toLowerCase();
    }

    @Override
    public void setContentType(@NotNull String contentType) {
        set("ct", contentType);
    }

    @Override
    public @NotNull MessageType getType() {
        return type;
    }

    @Override
    public void setType(@NotNull MessageType type) {
        this.type = type;
    }

    @Override
    public Encryption getEncryption() {
        return encryption;
    }

    @Override
    public void setEncryption(Encryption encryption) {
        this.encryption = encryption;
    }

    public static HeadersBuilder getFromBytes(byte @NotNull [] data) throws NoSuchEncryptionException, NoSuchReqHandler {
        byte flags = data[0];

        if (data.length < 3) {
            throw new IllegalArgumentException("Data is too short to extract the required information");
        }

        HeadersBuilder headersBuilder = new HeadersBuilder()
                .set(Connection.fromByte(flags))
                .set(FromByte.getMessageType(data[1]))
                .set(FromByte.getEncryptionString(data[2]));

//        boolean flag1 = (flags & 0b00100000) != 0;
//        boolean flag2 = (flags & 0b00010000) != 0;
//        boolean flag3 = (flags & 0b00001000) != 0;
//        boolean flag4 = (flags & 0b00000100) != 0;
//        boolean flag5 = (flags & 0b00000010) != 0;
//        boolean flag6 = (flags & 0b00000001) != 0;

        byte[] encoded = Arrays.copyOfRange(data, 3, data.length);
        String headersString = SimpleSix64.decode(encoded);

        for (String string : headersString.split(";")) {
            if (string.contains(":")) {
                String[] keyValue = string.split(":");
                headersBuilder.set(keyValue[0], keyValue[1]);
            }
        }

        return headersBuilder;
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        NodeType from = getConnection().getFrom();
        NodeType to = getConnection().getTo();
        byte first = 0;
        if (from == SERVER) first |= (byte) 0b10000000;
        if (to == SERVER) first |= (byte) 0b01000000;
        byteArrayOutputStream.write(first);
        byteArrayOutputStream.write(type.getByte());
        byteArrayOutputStream.write(FromByte.getEncryptionByte(getEncryption()));

        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, String> entry : this) {
            result
                    .append(entry.getKey())
                    .append(":")
                    .append(entry.getValue())
                    .append(";");
        }

        byte[] encoded = SimpleSix64.encode(result.toString());
        String ignored = SimpleSix64.decode(encoded);
        byteArrayOutputStream.write(encoded);

        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public @NotNull Iterator<Map.Entry<String, String>> iterator() {
        return map.entrySet().iterator();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Headers headers2)
            return map.equals(headers2.map);
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return map.toString();
    }
}
