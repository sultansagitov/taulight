package net.result.sandnode.message;

import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.exception.MessageSerializationException;
import net.result.sandnode.exception.crypto.NoSuchEncryptionException;
import net.result.sandnode.util.StreamReader;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public record EncryptedMessage(int version, byte encryptionByte, byte[] headersBytes, byte[] bodyBytes) {
    public static @NotNull EncryptedMessage readMessage(@NotNull InputStream in) {
        int version = StreamReader.readByte(in, "version");
        byte encryptionByte = StreamReader.readByte(in, "encryption");
        short headersLength = StreamReader.readShort(in, "headers length");
        byte[] headersBytes = StreamReader.readN(in, headersLength, "headers");
        int bodyLength = StreamReader.readInt(in, "body length");
        byte[] bodyBytes = StreamReader.readN(in, bodyLength, "body");
        return new EncryptedMessage(version, encryptionByte, headersBytes, bodyBytes);
    }

    public byte @NotNull [] toByteArray() {
        try {
            ByteArrayOutputStream result = new ByteArrayOutputStream();

            result.write(version);
            result.write(encryptionByte);

            int headersLength = headersBytes.length;

            result.write((headersLength >> 8) & 0xFF);
            result.write(headersLength & 0xFF);
            result.write(headersBytes);

            int bodyLength = bodyBytes.length;

            result.write((bodyLength >> 24) & 0xFF);
            result.write((bodyLength >> 16) & 0xFF);
            result.write((bodyLength >> 8) & 0xFF);
            result.write(bodyLength & 0xFF);
            result.write(bodyBytes);

            return result.toByteArray();
        } catch (IOException e) {
            throw new MessageSerializationException("Failed to serialize message", e);
        }
    }

    @Override
    public @NotNull String toString() {
        String encStr;
        try {
            encStr = EncryptionManager.find(encryptionByte).name();
        } catch (NoSuchEncryptionException e) {
            encStr = "%02X".formatted(encryptionByte);
        }
        String simpleName = getClass().getSimpleName();
        int headersLength = headersBytes.length;
        int bodyLength = bodyBytes.length;
        return "<%s v%d %s(headers %d) (body %d)>".formatted(simpleName, version, encStr, headersLength, bodyLength);
    }
}
