package net.result.sandnode.messages;

import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.DecryptionException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.protocol.FromByte;
import net.result.sandnode.util.encryption.Encryption;
import net.result.sandnode.util.encryption.EncryptionFactory;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import net.result.sandnode.util.encryption.interfaces.IDecryptor;
import net.result.sandnode.util.encryption.interfaces.IEncryptor;
import net.result.sandnode.util.encryption.interfaces.IKeyStorage;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

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

    public static @NotNull IMessage fromInput(@NotNull InputStream in, @NotNull GlobalKeyStorage globalKeyStorage) throws IOException, NoSuchEncryptionException, ReadingKeyException, NoSuchAlgorithmException, DecryptionException {
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
        byte[] decryptedBody = decryptBody(headers.encryption, bodyBytes, globalKeyStorage);

        return switch (headers.getType()) {
            case MESSAGE -> new JSONMessage(headersBuilder, new JSONObject(new String(decryptedBody)));
            case EXIT -> new EXITMessage(headersBuilder);
            default -> throw new IllegalStateException("Unexpected value: " + headers.getType());
        };
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
            Encryption bodyEncryption = getHeaders().encryption;
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
}
