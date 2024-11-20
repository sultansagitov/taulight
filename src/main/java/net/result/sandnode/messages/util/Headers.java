package net.result.sandnode.messages.util;

import net.result.sandnode.exceptions.NoSuchReqHandler;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.util.encryption.Encryptions;
import net.result.sandnode.util.encryption.interfaces.IEncryption;
import net.result.simplesix64.SimpleSix64;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static net.result.sandnode.messages.util.NodeType.HUB;
import static net.result.sandnode.util.encryption.Encryption.NONE;

public class Headers {
    private final Map<String, String> map = new HashMap<>();
    private final Connection connection;
    private final IMessageType type;
    private IEncryption bodyEncryption = NONE;

    public Headers(@NotNull Connection connection, @NotNull IMessageType type) {
        this.connection = connection;
        this.type = type;
    }

    public static HeadersBuilder getFromBytes(byte @NotNull [] data) throws NoSuchEncryptionException,
            NoSuchReqHandler {

        if (data.length < 3) {
            throw new IllegalArgumentException("Data is too short to extract the required information");
        }

        HeadersBuilder headersBuilder = new HeadersBuilder()
                .set(Connection.fromByte(data[0]))
                .set(MessageTypes.getMessageType(data[1]))
                .set(Encryptions.find(data[2]));

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

    public Connection getConnection() {
        return connection;
    }

    public String get(@NotNull String headerName) {
        return map.get(headerName.trim().toLowerCase());
    }

    public void set(@NotNull String headerName, @NotNull String value) {
        map.put(headerName.trim().toLowerCase(), value.trim());
    }

    public @NotNull IMessageType getType() {
        return type;
    }

    public IEncryption getBodyEncryption() {
        return bodyEncryption;
    }

    public void setBodyEncryption(@NotNull IEncryption bodyEncryption) {
        this.bodyEncryption = bodyEncryption;
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        NodeType from = getConnection().getFrom();
        NodeType to = getConnection().getTo();
        byte first = 0;
        if (from == HUB) first |= (byte) 0b10000000;
        if (to == HUB) first |= (byte) 0b01000000;
        byteArrayOutputStream.write(first);
        byteArrayOutputStream.write(type.asByte());
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
        byteArrayOutputStream.write(encoded);

        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public String toString() {
        return map.toString();
    }
}
