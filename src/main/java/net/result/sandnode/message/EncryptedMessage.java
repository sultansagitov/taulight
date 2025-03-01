package net.result.sandnode.message;

import net.result.sandnode.exception.UnexpectedSocketDisconnectException;
import net.result.sandnode.exception.crypto.NoSuchEncryptionException;
import net.result.sandnode.util.StreamReader;
import net.result.sandnode.encryption.EncryptionManager;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

public class EncryptedMessage {
    public final int version;
    public final byte encryptionByte;
    public final byte[] headersBytes;
    public final byte[] bodyBytes;

    private EncryptedMessage(int version, byte encryptionByte, byte[] headersBytes, byte[] bodyBytes) {
        this.version = version;
        this.encryptionByte = encryptionByte;
        this.headersBytes = headersBytes;
        this.bodyBytes = bodyBytes;
    }

    public static @NotNull EncryptedMessage readMessage(@NotNull InputStream in)
            throws UnexpectedSocketDisconnectException {
        int version = StreamReader.readByte(in, "version");
        byte encryptionByte = StreamReader.readByte(in, "encryption");
        short headersLength = StreamReader.readShort(in, "headers length");
        byte[] headersBytes = StreamReader.readN(in, headersLength, "headers");
        int bodyLength = StreamReader.readInt(in, "body length");
        byte[] bodyBytes = StreamReader.readN(in, bodyLength, "body");
        return new EncryptedMessage(version, encryptionByte, headersBytes, bodyBytes);
    }

    @Override
    public String toString() {
        String encStr;
        try {
            encStr = EncryptionManager.find(encryptionByte).name();
        } catch (NoSuchEncryptionException e) {
            encStr = "%02X".formatted(encryptionByte);
        }
        return "<%s v%d %s(headers %d) (body %d)>".formatted(
            getClass().getSimpleName(),
            version,
            encStr,
            headersBytes.length,
            bodyBytes.length
        );
    }
}
