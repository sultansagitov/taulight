package net.result.sandnode.message;

import net.result.sandnode.compression.Compression;
import net.result.sandnode.compression.CompressionManager;
import net.result.sandnode.compression.Compressions;
import net.result.sandnode.encryption.Encryptions;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.crypto.*;
import net.result.sandnode.exception.error.*;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.KeyStorageRegistry;
import net.result.sandnode.encryption.interfaces.Encryption;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

public abstract class Message implements IMessage {
    private static final Logger LOGGER = LogManager.getLogger(Message.class);
    private Encryption headersEncryption = Encryptions.NONE;
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

    public static @NotNull RawMessage decryptMessage(EncryptedMessage encrypted, KeyStorageRegistry keyStorageRegistry)
            throws DecryptionException, NoSuchMessageTypeException, NoSuchEncryptionException,
            KeyStorageNotFoundException, WrongKeyException, PrivateKeyNotFoundException {
        Encryption headersEncryption = EncryptionManager.find(encrypted.encryptionByte);
        byte[] decryptedHeaders;
        KeyStorage headersKeyStorage = keyStorageRegistry.getNonNull(headersEncryption);
        try {
            decryptedHeaders = headersEncryption.decryptBytes(encrypted.headersBytes, headersKeyStorage);
        } catch (CannotUseEncryption e) {
            throw new ImpossibleRuntimeException(e);
        }

        Headers headers = Headers.fromBytes(decryptedHeaders);

        Encryption bodyEncryption = headers.bodyEncryption();
        byte[] decryptedBody;
        KeyStorage bodyKeyStorage = keyStorageRegistry.getNonNull(bodyEncryption);
        try {
            decryptedBody = bodyEncryption.decryptBytes(encrypted.bodyBytes, bodyKeyStorage);
        } catch (CannotUseEncryption e) {
            throw new ImpossibleRuntimeException(e);
        }

        Optional<Compression> compression = CompressionManager.instance().getFromHeaders(headers);

        byte[] decompressed;
        try {
            decompressed = compression.orElse(Compressions.NONE).decompress(decryptedBody);
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
    public byte[] toByteArray(@NotNull KeyStorageRegistry keyStorageRegistry)
            throws EncryptionException, MessageSerializationException, IllegalMessageLengthException,
            KeyStorageNotFoundException, CryptoException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        result.write(1); // Version
        result.write(headersEncryption().asByte()); // Headers encryption

        byte[] encryptedHeaders;
        byte[] encryptedBody;

        short headersLength;
        int bodyLength;

        byte[] bodyBytes = getBody();
        Encryption bodyEncryption = headers().bodyEncryption();
        KeyStorage bodyKeyStorage = keyStorageRegistry.getNonNull(bodyEncryption);

        Optional<Compression> compression = CompressionManager.instance().getFromHeaders(headers);

        byte[] compressed;
        try {
            if (compression.isPresent()) {
                compressed = compression.get().compress(bodyBytes);
            } else {
                LOGGER.info("{} is not set in headers", CompressionManager.HEADER_NAME);
                Compressions defaultCompression = Compressions.DEFLATE;
                compressed = defaultCompression.compress(bodyBytes);

                if (compressed.length <= bodyBytes.length) {
                    headers.setValue(CompressionManager.HEADER_NAME, defaultCompression.name());
                } else {
                    LOGGER.info("{} made worse", defaultCompression.name());
                    compressed = bodyBytes;
                    headers.setValue(CompressionManager.HEADER_NAME, Compressions.NONE.name());
                }
            }
        } catch (IOException e) {
            compressed = bodyBytes;
            headers.setValue(CompressionManager.HEADER_NAME, Compressions.NONE.name());
        }

        try {
            encryptedBody = bodyEncryption.encryptBytes(compressed, bodyKeyStorage);
        } catch (CannotUseEncryption e) {
            throw new ImpossibleRuntimeException(e);
        }

        bodyLength = encryptedBody.length;


        try {
            byte[] headersBytes = headers().toByteArray();
            KeyStorage headersKeyStorage = keyStorageRegistry.getNonNull(headersEncryption());
            try {
                encryptedHeaders = headersEncryption().encryptBytes(headersBytes, headersKeyStorage);
            } catch (CannotUseEncryption e) {
                throw new ImpossibleRuntimeException(e);
            }

            int lengthInt = encryptedHeaders.length;
            if (lengthInt > 65535) throw new IllegalMessageLengthException(this, lengthInt);
            headersLength = (short) lengthInt;
        } catch (HeadersSerializationException | NullPointerException e) {
            throw new MessageSerializationException(this, e);
        }

        try {
            result.write((headersLength >> 8) & 0xFF);
            result.write(headersLength & 0xFF);
            result.write(encryptedHeaders);

            result.write((bodyLength >> 24) & 0xFF);
            result.write((bodyLength >> 16) & 0xFF);
            result.write((bodyLength >> 8) & 0xFF);
            result.write(bodyLength & 0xFF);
            result.write(encryptedBody);
        } catch (IOException e) {
            throw new MessageSerializationException(this, "Failed to serialize message", e);
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
            return "<%s %s(headers %s %s cid=%04X %s) %s(body %d)>".formatted(
                    getClass().getSimpleName(),
                    headersEncryption(),
                    headers().type().name(),
                    headers().connection().name(),
                    headers().chainID(),
                    headers().keys(),
                    headers().bodyEncryption().name(),
                    getBody().length
            );
        } catch (NullPointerException e) {
            return "<%s>".formatted(getClass().getSimpleName());
        }
    }
}
