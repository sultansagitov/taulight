package net.result.sandnode.messages;

import net.result.sandnode.exceptions.FirstByteEOFException;
import net.result.sandnode.exceptions.NoSuchReqHandler;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.DecryptionException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.util.encryption.Encryption;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import net.result.sandnode.util.encryption.core.interfaces.IEncryptor;
import net.result.sandnode.util.encryption.core.interfaces.IKeyStorage;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public abstract class Message implements IMessage {
    private final HeadersBuilder headersBuilder;

    public Message(@NotNull HeadersBuilder headersBuilder) {
        this.headersBuilder = headersBuilder;
    }

    public static @NotNull RawMessage fromInput(
            @NotNull InputStream in,
            @NotNull GlobalKeyStorage globalKeyStorage
    ) throws NoSuchEncryptionException, ReadingKeyException, DecryptionException, NoSuchReqHandler, EOFException {
        int versionInt;
        try {
            versionInt = in.read();
        } catch (IOException e) {
            throw new FirstByteEOFException("End of stream reached while reading version");
        }
        if (versionInt == -1) throw new FirstByteEOFException("End of stream reached while reading version");
        byte version = (byte) versionInt;

        int encryptionInt;
        try {
            encryptionInt = in.read();
        } catch (IOException e) {
            encryptionInt = -1;
        }
        if (encryptionInt == -1) throw new EOFException("End of stream reached while reading encryptionInt");
        byte encryptionByte = (byte) encryptionInt;

        byte[] headersLengthBytes;
        try {
            headersLengthBytes = in.readNBytes(2);
        } catch (IOException e) {
            headersLengthBytes = new byte[]{};
        }
        if (headersLengthBytes.length < 2) throw new EOFException("End of stream reached while reading headers length");
        short headersLength = ByteBuffer.wrap(headersLengthBytes).getShort();

        byte[] headersBytes;
        try {
            headersBytes = in.readNBytes(headersLength);
        } catch (IOException e) {
            headersBytes = new byte[]{};
        }
        if (headersBytes.length < headersLength) throw new EOFException("End of stream reached while reading headers");

        byte[] bodyLengthBytes;
        try {
            bodyLengthBytes = in.readNBytes(4);
        } catch (IOException e) {
            bodyLengthBytes = new byte[]{};
        }
        if (bodyLengthBytes.length < 4) throw new EOFException("End of stream reached while reading body length");
        int bodyLength = ByteBuffer.wrap(bodyLengthBytes).getInt();

        byte[] bodyBytes;
        try {
            bodyBytes = in.readNBytes(bodyLength);
        } catch (IOException e) {
            bodyBytes = new byte[]{};
        }
        if (bodyBytes.length < bodyLength) throw new EOFException("End of stream reached while reading body");


        Encryption headersEncryption = Encryption.fromByte(encryptionByte);
        byte[] decryptedHeaders = decryptHeaders(headersEncryption, headersBytes, globalKeyStorage);
        HeadersBuilder headersBuilder = Headers.getFromBytes(decryptedHeaders);
        Headers headers = headersBuilder.build();

        Encryption bodyEncryption = headers.getBodyEncryption();
        byte[] decryptedBody = decryptBody(bodyEncryption, bodyBytes, globalKeyStorage);

        return new RawMessage(headersBuilder, decryptedBody);
    }

    private static byte @NotNull [] decryptHeaders(
            @NotNull Encryption encryption,
            byte @NotNull [] encryptedHeaders,
            @NotNull GlobalKeyStorage globalKeyStorage
    ) throws DecryptionException, ReadingKeyException {
        IKeyStorage keyStorage = globalKeyStorage.get(encryption);
        return encryption.decryptor().decryptBytes(encryptedHeaders, keyStorage);
    }

    private static byte[] decryptBody(
            @NotNull Encryption encryption,
            byte @NotNull [] encryptedBody,
            @NotNull GlobalKeyStorage globalKeyStorage
    ) throws DecryptionException, ReadingKeyException {
        IKeyStorage keyStorage = globalKeyStorage.get(encryption);
        return encryption.decryptor().decryptBytes(encryptedBody, keyStorage);
    }

    @Override
    public byte[] toByteArray(
            @NotNull GlobalKeyStorage globalKeyStorage,
            @NotNull Encryption encryption
    ) throws IOException, ReadingKeyException, EncryptionException {
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
            Encryption bodyEncryption = getHeaders().getBodyEncryption();
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
                "<%s %s %s %s %s>",
                getClass().getSimpleName(),
                getHeaders().getBodyEncryption(),
                getHeaders().getType().name(),
                getHeaders().getConnection().name(),
                getHeaders().getContentType()
        );
    }
}
