package net.result.sandnode.messages;

import net.result.sandnode.exceptions.UnexpectedSocketDisconnectException;
import net.result.sandnode.exceptions.NoSuchEncryptionException;
import net.result.sandnode.util.StreamReader;
import net.result.sandnode.encryption.Encryptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

public class EncryptedMessage {
    private static final Logger LOGGER = LogManager.getLogger(EncryptedMessage.class);
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
        EncryptedMessage encrypted = new EncryptedMessage(version, encryptionByte, headersBytes, bodyBytes);
        LOGGER.info("(encrypted) Requested by {}", encrypted.toString());
        return encrypted;
    }

    @Override
    public String toString() {
        String encStr;
        try {
            encStr = Encryptions.find(encryptionByte).name();
        } catch (NoSuchEncryptionException e) {
            encStr = String.format("%02X", encryptionByte);
        }
        return String.format(
            "<%s v%d h%d-%s b%d>",
            getClass().getSimpleName(),
            version,
            headersBytes.length,
            encStr,
            bodyBytes.length
        );
    }
}
