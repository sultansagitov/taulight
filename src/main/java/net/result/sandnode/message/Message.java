package net.result.sandnode.message;

import net.result.sandnode.exception.*;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.GlobalKeyStorage;
import net.result.sandnode.encryption.interfaces.IEncryption;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static net.result.sandnode.encryption.Encryption.NONE;

public abstract class Message implements IMessage {
    private static final Logger LOGGER = LogManager.getLogger(Message.class);
    private IEncryption headersEncryption = NONE;
    private final Headers headers;

    public Message(@NotNull Headers headers) {
        this.headers = headers.copy();
    }

    @Override
    public void setHeadersEncryption(@NotNull IEncryption encryption) {
        headersEncryption = encryption;
    }

    @Override
    public @NotNull IEncryption getHeadersEncryption() {
        return headersEncryption;
    }

    public static @NotNull RawMessage decryptMessage(
            EncryptedMessage encrypted,
            GlobalKeyStorage globalKeyStorage
    ) throws DecryptionException, NoSuchMessageTypeException, NoSuchEncryptionException, KeyStorageNotFoundException,
            WrongKeyException, PrivateKeyNotFoundException {
        IEncryption headersEncryption = EncryptionManager.find(encrypted.encryptionByte);
        byte[] decryptedHeaders;
        KeyStorage headersKeyStorage = globalKeyStorage.getNonNull(headersEncryption);
        try {
            decryptedHeaders = headersEncryption.decryptBytes(encrypted.headersBytes, headersKeyStorage);
        } catch (CannotUseEncryption e) {
            throw new ImpossibleRuntimeException(e);
        }

        Headers headers = Headers.getFromBytes(decryptedHeaders);

        IEncryption bodyEncryption = headers.getBodyEncryption();
        byte[] decryptedBody;
        KeyStorage bodyKeyStorage = globalKeyStorage.getNonNull(bodyEncryption);
        try {
            decryptedBody = bodyEncryption.decryptBytes(encrypted.bodyBytes, bodyKeyStorage);
        } catch (CannotUseEncryption e) {
            throw new ImpossibleRuntimeException(e);
        }

        RawMessage request = new RawMessage(headers, decryptedBody);
        request.setHeadersEncryption(headersEncryption);
        LOGGER.info("Requested by {}", request);
        return request;
    }

    @Override
    public byte[] toByteArray(@NotNull GlobalKeyStorage globalKeyStorage)
            throws EncryptionException, MessageSerializationException, IllegalMessageLengthException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        IEncryption encryption = getHeadersEncryption();
        byteArrayOutputStream.write(1); // Version
        byteArrayOutputStream.write(encryption.asByte()); // Headers encryption

        try {
            byte[] headersBytes = getHeaders().toByteArray();
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
            byteArrayOutputStream.write((length >> 8) & 0xFF);
            byteArrayOutputStream.write(length & 0xFF);
            byteArrayOutputStream.write(encryptedHeaders);
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
            IEncryption bodyEncryption = getHeaders().getBodyEncryption();
            KeyStorage keyStorage = globalKeyStorage.getNonNull(bodyEncryption);
            byte[] encryptionBody;
            try {
                encryptionBody = bodyEncryption.encryptBytes(bodyBytes, keyStorage);
            } catch (CannotUseEncryption e) {
                throw new ImpossibleRuntimeException(e);
            }

            int length = encryptionBody.length;
            byteArrayOutputStream.write((length >> 24) & 0xFF);
            byteArrayOutputStream.write((length >> 16) & 0xFF);
            byteArrayOutputStream.write((length >> 8) & 0xFF);
            byteArrayOutputStream.write(length & 0xFF);
            byteArrayOutputStream.write(encryptionBody);
        } catch (IOException e) {
            throw new MessageSerializationException(this, "Failed to serialize message body", e);
        }

        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public Headers getHeaders() {
        return headers;
    }

    @Override
    public String toString() {
        try {
            return "<%s %s(headers %s %s cid=%04X %d) %s(body %d)>".formatted(
                    getClass().getSimpleName(),
                    getHeadersEncryption(),
                    getHeaders().getType().name(),
                    getHeaders().getConnection().name(),
                    getHeaders().getChainID(),
                    getHeaders().getCount(),
                    getHeaders().getBodyEncryption().name(),
                    getBody().length
            );
        } catch (NullPointerException e) {
            return "<%s>".formatted(getClass().getSimpleName());
        }
    }
}
