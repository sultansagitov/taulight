package net.result.sandnode.messages;

import net.result.sandnode.exceptions.*;
import net.result.sandnode.messages.util.Headers;
import net.result.sandnode.encryption.Encryptions;
import net.result.sandnode.encryption.GlobalKeyStorage;
import net.result.sandnode.encryption.interfaces.IEncryption;
import net.result.sandnode.encryption.interfaces.IEncryptor;
import net.result.sandnode.encryption.interfaces.IKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract class Message implements IMessage {
    private static final Logger LOGGER = LogManager.getLogger(Message.class);
    private final Headers headers;

    public Message(@NotNull Headers headers) {
        this.headers = headers;
    }

    public static @NotNull RawMessage decryptMessage(
            @NotNull GlobalKeyStorage globalKeyStorage,
            @NotNull EncryptedMessage encrypted
    ) throws DecryptionException, NoSuchMessageTypeException, NoSuchEncryptionException, KeyStorageNotFoundException {
        IEncryption headersEncryption = Encryptions.find(encrypted.encryptionByte);
        byte[] decryptedHeaders = decryptHeaders(headersEncryption, encrypted.headersBytes, globalKeyStorage);
        Headers headers = Headers.getFromBytes(decryptedHeaders);

        IEncryption bodyEncryption = headers.getBodyEncryption();
        byte[] decryptedBody = decryptBody(bodyEncryption, encrypted.bodyBytes, globalKeyStorage);

        RawMessage request = new RawMessage(headers, decryptedBody);
        LOGGER.info("(decrypted) Requested by {} {}", request.getHeaders().getBodyEncryption().name(), request);
        return request;
    }

    private static byte @NotNull [] decryptHeaders(
            @NotNull IEncryption encryption,
            byte @NotNull [] encryptedHeaders,
            @NotNull GlobalKeyStorage globalKeyStorage
    ) throws DecryptionException, KeyStorageNotFoundException {
        IKeyStorage keyStorage = globalKeyStorage.getNonNull(encryption);
        return encryption.decryptor().decryptBytes(encryptedHeaders, keyStorage);
    }

    private static byte[] decryptBody(
            @NotNull IEncryption encryption,
            byte @NotNull [] encryptedBody,
            @NotNull GlobalKeyStorage globalKeyStorage
    ) throws DecryptionException, KeyStorageNotFoundException {
        IKeyStorage keyStorage = globalKeyStorage.getNonNull(encryption);
        return encryption.decryptor().decryptBytes(encryptedBody, keyStorage);
    }

    @Override
    public byte[] toByteArray(
            @NotNull GlobalKeyStorage globalKeyStorage,
            @NotNull IEncryption encryption
    ) throws EncryptionException, KeyStorageNotFoundException, MessageSerializationException,
            IllegalMessageLengthException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(1); // Version
        byteArrayOutputStream.write(encryption.asByte()); // Headers encryption

        try {
            byte[] headersBytes = getHeaders().toByteArray();
            IKeyStorage keyStorage = globalKeyStorage.getNonNull(encryption);
            byte[] encryptedHeaders = encryption.encryptor().encryptBytes(headersBytes, keyStorage);

            int lengthInt = encryptedHeaders.length;
            if (lengthInt > Short.MAX_VALUE) {
                throw new IllegalMessageLengthException("Header length exceeds Short.MAX_VALUE: " + lengthInt);
            }
            short length = (short) lengthInt;
            byteArrayOutputStream.write((length >> 8) & 0xFF);
            byteArrayOutputStream.write(length & 0xFF);
            byteArrayOutputStream.write(encryptedHeaders);
        } catch (IOException e) {
            throw new MessageSerializationException("Failed to serialize message headers", e);
        }

        try {
            byte[] bodyBytes = getBody();
            IEncryption bodyEncryption = getHeaders().getBodyEncryption();
            IEncryptor encryptor = bodyEncryption.encryptor();
            IKeyStorage keyStorage = globalKeyStorage.getNonNull(bodyEncryption);
            byte[] encryptionBody = encryptor.encryptBytes(bodyBytes, keyStorage);

            int length = encryptionBody.length;
            byteArrayOutputStream.write((length >> 24) & 0xFF);
            byteArrayOutputStream.write((length >> 16) & 0xFF);
            byteArrayOutputStream.write((length >> 8) & 0xFF);
            byteArrayOutputStream.write(length & 0xFF);
            byteArrayOutputStream.write(encryptionBody);
        } catch (IOException e) {
            throw new MessageSerializationException("Failed to serialize message body", e);
        }

        return byteArrayOutputStream.toByteArray();
    }


    @Override
    public Headers getHeaders() {
        return headers;
    }

    @Override
    public String toString() {
        return String.format(
                "<%s e=%s t=%s c=%s>",
                getClass().getSimpleName(),
                getHeaders().getBodyEncryption().name(),
                getHeaders().getType().name(),
                getHeaders().getConnection().name()
        );
    }

}
