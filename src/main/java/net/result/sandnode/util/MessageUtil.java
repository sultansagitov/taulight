package net.result.sandnode.util;

import net.result.sandnode.compression.Compression;
import net.result.sandnode.compression.CompressionManager;
import net.result.sandnode.compression.Compressions;
import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.KeyStorageRegistry;
import net.result.sandnode.encryption.interfaces.Encryption;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.crypto.*;
import net.result.sandnode.exception.error.DecryptionException;
import net.result.sandnode.exception.error.EncryptionException;
import net.result.sandnode.exception.error.KeyStorageNotFoundException;
import net.result.sandnode.message.EncryptedMessage;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Optional;

public class MessageUtil {
    private static final Logger LOGGER = LogManager.getLogger(MessageUtil.class);

    public static @NotNull RawMessage decryptMessage(EncryptedMessage encrypted, KeyStorageRegistry keyStorageRegistry)
            throws DecryptionException, NoSuchMessageTypeException, NoSuchEncryptionException,
            KeyStorageNotFoundException, WrongKeyException, PrivateKeyNotFoundException {
        Encryption headersEncryption = EncryptionManager.find(encrypted.encryptionByte());
        byte[] decryptedHeaders;
        KeyStorage headersKeyStorage = keyStorageRegistry.getNonNull(headersEncryption);
        try {
            decryptedHeaders = headersEncryption.decryptBytes(encrypted.headersBytes(), headersKeyStorage);
        } catch (CannotUseEncryption e) {
            throw new ImpossibleRuntimeException(e);
        }

        Headers headers = Headers.fromBytes(decryptedHeaders);

        Encryption bodyEncryption = headers.bodyEncryption();
        byte[] decryptedBody;
        KeyStorage bodyKeyStorage = keyStorageRegistry.getNonNull(bodyEncryption);
        try {
            decryptedBody = bodyEncryption.decryptBytes(encrypted.bodyBytes(), bodyKeyStorage);
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

    public static EncryptedMessage encryptMessage(Message message, KeyStorageRegistry keyStorageRegistry)
            throws KeyStorageNotFoundException, EncryptionException, CryptoException, IllegalMessageLengthException,
            MessageSerializationException {
        byte[] encryptedHeaders;
        byte[] encryptedBody;

        byte[] bodyBytes = message.getBody();
        Encryption bodyEncryption = message.headers().bodyEncryption();
        KeyStorage bodyKeyStorage = keyStorageRegistry.getNonNull(bodyEncryption);

        Optional<Compression> compression = CompressionManager.instance().getFromHeaders(message.headers());

        byte[] compressed;
        try {
            if (compression.isPresent()) {
                compressed = compression.get().compress(bodyBytes);
            } else {
                Compressions defaultCompression = Compressions.DEFLATE;
                compressed = defaultCompression.compress(bodyBytes);

                if (compressed.length <= bodyBytes.length) {
                    message.headers().setValue(CompressionManager.HEADER_NAME, defaultCompression.name());
                } else {
                    LOGGER.info("{} made worse", defaultCompression.name());
                    compressed = bodyBytes;
                    message.headers().setValue(CompressionManager.HEADER_NAME, Compressions.NONE.name());
                }
            }
        } catch (IOException e) {
            compressed = bodyBytes;
            message.headers().setValue(CompressionManager.HEADER_NAME, Compressions.NONE.name());
        }

        try {
            encryptedBody = bodyEncryption.encryptBytes(compressed, bodyKeyStorage);
        } catch (CannotUseEncryption e) {
            throw new ImpossibleRuntimeException(e);
        }

        try {
            byte[] headersBytes = message.headers().toByteArray();
            KeyStorage headersKeyStorage = keyStorageRegistry.getNonNull(message.headersEncryption());
            try {
                encryptedHeaders = message.headersEncryption().encryptBytes(headersBytes, headersKeyStorage);
            } catch (CannotUseEncryption e) {
                throw new ImpossibleRuntimeException(e);
            }

            int lengthInt = encryptedHeaders.length;
            if (lengthInt > 65535) throw new IllegalMessageLengthException(message, lengthInt);
        } catch (HeadersSerializationException | NullPointerException e) {
            throw new MessageSerializationException(e);
        }

        return new EncryptedMessage(1, message.headersEncryption().asByte(), encryptedHeaders, encryptedBody);
    }
}
