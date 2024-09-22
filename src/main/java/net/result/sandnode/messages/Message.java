package net.result.sandnode.messages;

import net.result.sandnode.exceptions.NoSuchReqHandler;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.DecryptionException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.messages.util.Connection;
import net.result.sandnode.messages.util.MessageType;
import net.result.sandnode.protocol.FromByte;
import net.result.sandnode.util.encryption.Encryption;
import net.result.sandnode.util.encryption.EncryptionFactory;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import net.result.sandnode.util.encryption.interfaces.IDecryptor;
import net.result.sandnode.util.encryption.interfaces.IEncryptor;
import net.result.sandnode.util.encryption.interfaces.IKeyStorage;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;

public abstract class Message implements IMessage {
    protected final Headers headers;

    public Message(@NotNull HeadersBuilder headersBuilder) {
        this.headers = headersBuilder.build();
    }

    public static @NotNull RawMessage fromInput(
            @NotNull InputStream in,
            @NotNull GlobalKeyStorage globalKeyStorage
    ) throws IOException, NoSuchEncryptionException, ReadingKeyException, NoSuchAlgorithmException, DecryptionException, NoSuchReqHandler {
        byte version = (byte) in.read();
        byte encryptionInt = (byte) in.read();
        short headersLength = ByteBuffer.wrap(in.readNBytes(2)).getShort();
        byte[] headersBytes = in.readNBytes(headersLength);
        int bodyLength = ByteBuffer.wrap(in.readNBytes(4)).getInt();
        byte[] bodyBytes = in.readNBytes(bodyLength);

        Encryption encryptionType = FromByte.getEncryptionString(encryptionInt);
        byte[] decryptedHeaders = decryptHeaders(encryptionType, headersBytes, globalKeyStorage);
        HeadersBuilder headersBuilder = Headers.getFromBytes(decryptedHeaders);
        Headers headers = headersBuilder.build();
        byte[] decryptedBody = decryptBody(headers.getEncryption(), bodyBytes, globalKeyStorage);

        RawMessage rawMessage = new RawMessage(headersBuilder);
        rawMessage.setBody(decryptedBody);
        return rawMessage;
    }

    private static byte @NotNull [] decryptHeaders(
            @NotNull Encryption encryption,
            byte @NotNull [] encryptedHeaders,
            @NotNull GlobalKeyStorage globalKeyStorage
    ) throws DecryptionException, ReadingKeyException {
        final IDecryptor asymmetricDecryptor = EncryptionFactory.getDecryptor(encryption);
        final IKeyStorage rsaKeyStorage = globalKeyStorage.getRSAKeyStorage();
        return asymmetricDecryptor.decryptBytes(encryptedHeaders, rsaKeyStorage);
    }

    private static byte[] decryptBody(
            @NotNull Encryption encryption,
            byte @NotNull [] encryptedBody,
            @NotNull GlobalKeyStorage globalKeyStorage
    ) throws DecryptionException, ReadingKeyException {
        final IDecryptor aesDecryptor = EncryptionFactory.getDecryptor(encryption);
        final IKeyStorage keyStorage = EncryptionFactory.getKeyStorage(globalKeyStorage, encryption);
        return aesDecryptor.decryptBytes(encryptedBody, keyStorage);
    }

    @Override
    public @NotNull Headers getHeaders() {
        return headers;
    }

    @Override
    public byte[] toByteArray(Encryption encryption, GlobalKeyStorage globalKeyStorage) throws IOException, ReadingKeyException, EncryptionException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(1); // Version
        byteArrayOutputStream.write(FromByte.getEncryptionByte(encryption)); // Headers encryption

        {
            byte[] headersBytes = getHeaders().toByteArray();
            IEncryptor encryptor = EncryptionFactory.getEncryptor(encryption);
            IKeyStorage keyStorage = EncryptionFactory.getKeyStorage(globalKeyStorage, encryption);
            byte[] encryptedHeaders = encryptor.encryptBytes(headersBytes, keyStorage);
            short length = (short) encryptedHeaders.length;
            byteArrayOutputStream.write((length >> 8) & 0xFF);
            byteArrayOutputStream.write(length & 0xFF);
            byteArrayOutputStream.write(encryptedHeaders);
        }

        {
            byte[] bodyBytes = getBody();
            Encryption bodyEncryption = getEncryption();
            IEncryptor encryptor = EncryptionFactory.getEncryptor(bodyEncryption);
            IKeyStorage keyStorage = EncryptionFactory.getKeyStorage(globalKeyStorage, bodyEncryption);
            byte[] encryptionBody = encryptor.encryptBytes(bodyBytes, keyStorage);
            byteArrayOutputStream.write((encryptionBody.length >> 24) & 0xFF);
            byteArrayOutputStream.write((encryptionBody.length >> 16) & 0xFF);
            byteArrayOutputStream.write((encryptionBody.length >> 8) & 0xFF);
            byteArrayOutputStream.write((encryptionBody.length) & 0xFF);
            byteArrayOutputStream.write(encryptionBody);
        }

        return byteArrayOutputStream.toByteArray();
    }


    @Override
    public Connection getConnection() {
        return headers.getConnection();
    }

    @Override
    public @NotNull String getContentType() {
        return headers.getContentType();
    }

    @Override
    public void setContentType(@NotNull String contentType) {
        headers.setContentType(contentType);
    }

    @Override
    public void setType(@NotNull MessageType type) {
        headers.setType(type);
    }

    @Override
    public MessageType getType() {
        return headers.getType();
    }

    @Override
    public Encryption getEncryption() {
        return headers.getEncryption();
    }

    @Override
    public void setEncryption(Encryption encryption) {
        headers.setEncryption(encryption);
    }
}
