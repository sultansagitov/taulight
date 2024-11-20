package net.result.sandnode.messages;

import net.result.sandnode.exceptions.KeyStorageNotFoundException;
import net.result.sandnode.exceptions.NoSuchReqHandler;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.UnexpectedSocketDisconnect;
import net.result.sandnode.exceptions.encryption.DecryptionException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.messages.util.Headers;
import net.result.sandnode.messages.util.HeadersBuilder;
import net.result.sandnode.util.encryption.Encryptions;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import net.result.sandnode.util.encryption.interfaces.IEncryption;
import net.result.sandnode.util.encryption.interfaces.IEncryptor;
import net.result.sandnode.util.encryption.interfaces.IKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public abstract class Message implements IMessage {
    private static final Logger LOGGER = LogManager.getLogger(Message.class);
    private final HeadersBuilder headersBuilder;

    public Message(@NotNull HeadersBuilder headersBuilder) {
        this.headersBuilder = headersBuilder;
    }

    public static @NotNull Message.EncryptedMessage readMessage(@NotNull InputStream in)
            throws UnexpectedSocketDisconnect {
        int version = readByte(in);
        byte encryptionByte = readByte(in);
        short headersLength = readShort(in);
        byte[] headersBytes = readN(in, headersLength);
        int bodyLength = readInt(in);
        byte[] bodyBytes = readN(in, bodyLength);
        return new EncryptedMessage(version, encryptionByte, headersBytes, bodyBytes);
    }

    public static @NotNull RawMessage decryptMessage(
            @NotNull GlobalKeyStorage globalKeyStorage,
            @NotNull EncryptedMessage encrypted
    ) throws DecryptionException, ReadingKeyException, NoSuchReqHandler, NoSuchEncryptionException, KeyStorageNotFoundException {
        IEncryption headersEncryption = Encryptions.find(encrypted.encryptionByte);
        byte[] decryptedHeaders = decryptHeaders(headersEncryption, encrypted.headersBytes, globalKeyStorage);
        HeadersBuilder headersBuilder = Headers.getFromBytes(decryptedHeaders);
        Headers headers = headersBuilder.build();

        IEncryption bodyEncryption = headers.getBodyEncryption();
        byte[] decryptedBody = decryptBody(bodyEncryption, encrypted.bodyBytes, globalKeyStorage);

        RawMessage request = new RawMessage(headersBuilder, decryptedBody);
        LOGGER.info("Requested by {} {}", headersEncryption, request);
        return request;
    }

    private static byte readByte(@NotNull InputStream in) throws UnexpectedSocketDisconnect {
        int versionInt;
        try {
            versionInt = in.read();
        } catch (IOException e) {
            throw new UnexpectedSocketDisconnect();
        }
        if (versionInt == -1) throw new UnexpectedSocketDisconnect();
        return (byte) versionInt;
    }

    private static short readShort(@NotNull InputStream in) throws UnexpectedSocketDisconnect {
        byte[] bytes;
        try {
            bytes = in.readNBytes(2);
        } catch (IOException e) {
            bytes = new byte[]{};
        }
        if (bytes.length < 2) throw new UnexpectedSocketDisconnect();
        return ByteBuffer.wrap(bytes).getShort();
    }

    private static int readInt(@NotNull InputStream in) throws UnexpectedSocketDisconnect {
        byte[] bytes;
        try {
            bytes = in.readNBytes(4);
        } catch (IOException e) {
            throw new UnexpectedSocketDisconnect();
        }
        if (bytes.length < 4) throw new UnexpectedSocketDisconnect();
        return ByteBuffer.wrap(bytes).getInt();
    }

    private static byte @NotNull [] readN(@NotNull InputStream in, int length) throws UnexpectedSocketDisconnect {
        byte[] bytes;

        try {
            bytes = in.readNBytes(length);
        } catch (IOException e) {
            throw new UnexpectedSocketDisconnect();
        }

        if (bytes.length < length) {
            throw new UnexpectedSocketDisconnect();
        }

        return bytes;
    }

    private static byte @NotNull [] decryptHeaders(
            @NotNull IEncryption encryption,
            byte @NotNull [] encryptedHeaders,
            @NotNull GlobalKeyStorage globalKeyStorage
    ) throws DecryptionException, ReadingKeyException, KeyStorageNotFoundException {
        IKeyStorage keyStorage = globalKeyStorage.getNonNull(encryption);
        return encryption.decryptor().decryptBytes(encryptedHeaders, keyStorage);
    }

    private static byte[] decryptBody(
            @NotNull IEncryption encryption,
            byte @NotNull [] encryptedBody,
            @NotNull GlobalKeyStorage globalKeyStorage
    ) throws DecryptionException, ReadingKeyException, KeyStorageNotFoundException {
        IKeyStorage keyStorage = globalKeyStorage.getNonNull(encryption);
        return encryption.decryptor().decryptBytes(encryptedBody, keyStorage);
    }

    @Override
    public byte[] toByteArray(
            @NotNull GlobalKeyStorage globalKeyStorage,
            @NotNull IEncryption encryption
    ) throws IOException, ReadingKeyException, EncryptionException, KeyStorageNotFoundException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(1); // Version
        byteArrayOutputStream.write(encryption.asByte()); // Headers encryption

        {
            byte[] headersBytes = getHeaders().toByteArray();
            IEncryptor encryptor = encryption.encryptor();
            IKeyStorage keyStorage = globalKeyStorage.getNonNull(encryption);
            byte[] encryptedHeaders = encryptor.encryptBytes(headersBytes, keyStorage);
            int lengthInt = encryptedHeaders.length;
            if (lengthInt > Short.MAX_VALUE)
                throw new IllegalArgumentException("Length is too large for a short: " + lengthInt);
            short length = (short) lengthInt;
            byteArrayOutputStream.write((length >> 8) & 0xFF);
            byteArrayOutputStream.write(length & 0xFF);
            byteArrayOutputStream.write(encryptedHeaders);
        }

        {
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
        }

        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public HeadersBuilder getHeadersBuilder() {
        return headersBuilder;
    }

    @Override
    public @NotNull Headers getHeaders() {
        return getHeadersBuilder().build();
    }

    @Override
    public String toString() {
        return String.format(
                "<%s %s %s %s>",
                getClass().getSimpleName(),
                getHeaders().getBodyEncryption(),
                getHeaders().getType().name(),
                getHeaders().getConnection().name()
        );
    }

    public static class EncryptedMessage {
        public final int version;
        public final byte encryptionByte;
        public final byte[] headersBytes;
        public final byte[] bodyBytes;

        public EncryptedMessage(int version, byte encryptionByte, byte[] headersBytes, byte[] bodyBytes) {
            this.version = version;
            this.encryptionByte = encryptionByte;
            this.headersBytes = headersBytes;
            this.bodyBytes = bodyBytes;
        }
    }
}
