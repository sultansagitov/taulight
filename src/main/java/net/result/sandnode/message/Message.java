package net.result.sandnode.message;

import net.result.sandnode.compression.Compression;
import net.result.sandnode.compression.CompressionManager;
import net.result.sandnode.exception.*;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.GlobalKeyStorage;
import net.result.sandnode.encryption.interfaces.Encryption;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static net.result.sandnode.encryption.Encryptions.NONE;

public abstract class Message implements IMessage {
    private static final Logger LOGGER = LogManager.getLogger(Message.class);
    private Encryption headersEncryption = NONE;
    private final Headers headers;

    public Message(@NotNull Headers headers) {
        this.headers = headers.copy();
    }

    @Override
    public void setHeadersEncryption(@NotNull Encryption encryption) {
        headersEncryption = encryption;
    }

    @Override
    public @NotNull Encryption headersEncryption() {
        return headersEncryption;
    }

    public static @NotNull RawMessage decryptMessage(EncryptedMessage encrypted, GlobalKeyStorage globalKeyStorage)
            throws DecryptionException, NoSuchMessageTypeException, NoSuchEncryptionException,
            KeyStorageNotFoundException, WrongKeyException, PrivateKeyNotFoundException {
        Encryption headersEncryption = EncryptionManager.find(encrypted.encryptionByte);
        byte[] decryptedHeaders;
        KeyStorage headersKeyStorage = globalKeyStorage.getNonNull(headersEncryption);
        try {
            decryptedHeaders = headersEncryption.decryptBytes(encrypted.headersBytes, headersKeyStorage);
        } catch (CannotUseEncryption e) {
            throw new ImpossibleRuntimeException(e);
        }

        Headers headers = Headers.fromBytes(decryptedHeaders);

        Encryption bodyEncryption = headers.bodyEncryption();
        byte[] decryptedBody;
        KeyStorage bodyKeyStorage = globalKeyStorage.getNonNull(bodyEncryption);
        try {
            decryptedBody = bodyEncryption.decryptBytes(encrypted.bodyBytes, bodyKeyStorage);
        } catch (CannotUseEncryption e) {
            throw new ImpossibleRuntimeException(e);
        }

        Compression compression = CompressionManager.instance().getFromHeaders(headers);

        byte[] decompressed;
        try {
            decompressed = compression.decompress(decryptedBody);
        } catch (IOException e) {
            LOGGER.error("Using not decompressed body", e);
            decompressed = decryptedBody;
        }

        RawMessage request = new RawMessage(headers, decompressed);
        request.setHeadersEncryption(headersEncryption);
        LOGGER.info("Requested by {}", request);
        return request;
    }

    @Override
    public byte[] toByteArray(@NotNull GlobalKeyStorage globalKeyStorage)
            throws EncryptionException, MessageSerializationException, IllegalMessageLengthException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        Encryption encryption = headersEncryption();
        result.write(1); // Version
        result.write(encryption.asByte()); // Headers encryption

        try {
            byte[] headersBytes = headers().toByteArray();
            KeyStorage keyStorage = globalKeyStorage.getNonNull(encryption);
            byte[] encryptedHeaders;
            try {
                encryptedHeaders = encryption.encryptBytes(headersBytes, keyStorage);
            } catch (CannotUseEncryption e) {
                throw new ImpossibleRuntimeException(e);
            }

            int lengthInt = encryptedHeaders.length;
            if (lengthInt > 65535) {
                throw new IllegalMessageLengthException(this, "Header length exceeds 65535: %d".formatted(lengthInt));
            }
            short length = (short) lengthInt;
            result.write((length >> 8) & 0xFF);
            result.write(length & 0xFF);
            result.write(encryptedHeaders);
        } catch (IOException e) {
            throw new MessageSerializationException(this, "Failed to serialize message headers", e);
        } catch (HeadersSerializationException | NullPointerException e) {
            throw new MessageSerializationException(
                    this,
                    "%s: %s".formatted(e.getClass().getSimpleName(), e.getMessage()),
                    e
            );
        }

        try {
            byte[] bodyBytes = getBody();
            Encryption bodyEncryption = headers().bodyEncryption();
            KeyStorage keyStorage = globalKeyStorage.getNonNull(bodyEncryption);
            byte[] encryptedBody;

            Compression compression = CompressionManager.instance().getFromHeaders(headers);

            byte[] compressed = compression.compress(bodyBytes);
            try {
                encryptedBody = bodyEncryption.encryptBytes(compressed, keyStorage);
            } catch (CannotUseEncryption e) {
                throw new ImpossibleRuntimeException(e);
            }

            int length = encryptedBody.length;
            result.write((length >> 24) & 0xFF);
            result.write((length >> 16) & 0xFF);
            result.write((length >> 8) & 0xFF);
            result.write(length & 0xFF);
            result.write(encryptedBody);
        } catch (IOException e) {
            throw new MessageSerializationException(this, "Failed to serialize message body", e);
        }

        return result.toByteArray();
    }

    @Override
    public Headers headers() {
        return headers;
    }

    @Override
    public String toString() {
        try {
            return "<%s %s(headers %s %s cid=%04X %d) %s(body %d)>".formatted(
                    getClass().getSimpleName(),
                    headersEncryption(),
                    headers().type().name(),
                    headers().connection().name(),
                    headers().chainID(),
                    headers().count(),
                    headers().bodyEncryption().name(),
                    getBody().length
            );
        } catch (NullPointerException e) {
            return "<%s>".formatted(getClass().getSimpleName());
        }
    }
}
