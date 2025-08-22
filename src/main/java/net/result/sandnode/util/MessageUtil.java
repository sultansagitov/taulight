package net.result.sandnode.util;

import net.result.sandnode.compression.Compression;
import net.result.sandnode.compression.CompressionManager;
import net.result.sandnode.compression.Compressions;
import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.KeyStorageRegistry;
import net.result.sandnode.encryption.interfaces.Encryption;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.crypto.CannotUseEncryption;
import net.result.sandnode.message.EncryptedMessage;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class MessageUtil {
    private static final Logger LOGGER = LogManager.getLogger(MessageUtil.class);

    public static RawMessage decryptMessage(EncryptedMessage encrypted, KeyStorageRegistry keyStorageRegistry) {
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
        } catch (DeserializationException e) {
            LOGGER.error("Using not decompressed body", e);
            decompressed = decryptedBody;
        }

        RawMessage message = new RawMessage(headers, decompressed);
        message.setHeadersEncryption(headersEncryption);
        LOGGER.info("Received {}", message);
        return message;
    }

    public static EncryptedMessage encryptMessage(Message message, KeyStorageRegistry keyStorageRegistry) {
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
        } catch (DeserializationException e) {
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
            if (lengthInt > 65535) throw new IllegalMessageLengthException(lengthInt);
        } catch (HeadersSerializationException | NullPointerException e) {
            throw new MessageSerializationException(e);
        }

        return new EncryptedMessage(1, message.headersEncryption().asByte(), encryptedHeaders, encryptedBody);
    }
}
