package net.result.sandnode.client;

import net.result.sandnode.exceptions.*;
import net.result.sandnode.exceptions.encryption.CannotUseEncryption;
import net.result.sandnode.exceptions.encryption.DecryptionException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.messages.HeadersBuilder;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.Message;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.util.encryption.Encryption;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import net.result.sandnode.util.encryption.aes.AESGenerator;
import net.result.sandnode.util.encryption.asymmetric.Asymmetric;
import net.result.sandnode.util.encryption.asymmetric.AsymmetricKeyStorage;
import net.result.sandnode.util.encryption.asymmetric.interfaces.IAsymmetricConvertor;
import net.result.sandnode.util.encryption.symmetric.aes.AESKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

import static net.result.sandnode.messages.util.Connection.CLIENT2SERVER;
import static net.result.sandnode.messages.util.MessageType.PUBLICKEY;
import static net.result.sandnode.messages.util.MessageType.SYMKEY;
import static net.result.sandnode.util.encryption.Encryption.AES;
import static net.result.sandnode.util.encryption.Encryption.NO;

public class Client {
    private static final Logger LOGGER = LogManager.getLogger(Client.class);

    private final String serverAddress;
    private final int port;
    private Socket socket;
    private OutputStream out;
    private InputStream in;
    private final @NotNull GlobalKeyStorage clientKeyStorage;
    private Encryption encryptionOfServer;

    public Client(@NotNull String serverAddress, int port, @NotNull GlobalKeyStorage clientKeyStorage) {
        this.serverAddress = serverAddress;
        this.port = port;
        this.clientKeyStorage = clientKeyStorage;
    }

    public void connect() {
        try {
            LOGGER.info("Connecting to {}:{}", serverAddress, port);
            socket = new Socket(serverAddress, port);

            in = socket.getInputStream();
            out = socket.getOutputStream();
            LOGGER.info("Connection established.");
        } catch (IOException e) {
            LOGGER.error("Error connecting to server", e);
            close();
        }
    }

    public void sendMessage(@NotNull IMessage message, @NotNull Encryption headersEncryption) throws IOException, ReadingKeyException, EncryptionException {
        if (isConnected()) {
            out.write(message.toByteArray(clientKeyStorage, headersEncryption));
            out.flush();
            LOGGER.info("Message sent: {}", message);
        }
    }

    public @Nullable IMessage receiveMessage() throws NoSuchEncryptionException, ReadingKeyException, NoSuchAlgorithmException, DecryptionException, NoSuchReqHandler {
        if (isConnected()) {
            try {
                RawMessage response = Message.fromInput(in, clientKeyStorage);
                LOGGER.info("Received response: {}", response);
                return response;
            } catch (FirstByteEOFException ignored) {
            } catch (IOException e) {
                LOGGER.error("Error receiving response", e);
            }
        }
        return null;
    }

    public void close() {
        try {
            if (socket != null) {
                socket.close();
                LOGGER.info("Connection closed.");
            }
        } catch (IOException e) {
            LOGGER.error("Error closing connection", e);
        }
    }

    public boolean isConnected() {
        return socket != null && !socket.isClosed();
    }

    public Encryption getPublicKeyFromServer() throws
            ReadingKeyException, EncryptionException, IOException, NoSuchEncryptionException, DecryptionException,
            NoSuchReqHandler, CreatingKeyException, CannotUseEncryption, NoSuchAlgorithmException {
        HeadersBuilder headersBuilder = new HeadersBuilder()
                .set(CLIENT2SERVER)
                .set(PUBLICKEY);
        RawMessage request = new RawMessage(headersBuilder);
        request.setBody(new byte[0]);
        sendMessage(request, NO);

        IMessage response = receiveMessage();
        String encryptionByteString = response.getHeaders().get("encryption");
        Encryption encryption = Encryption.fromByte(Byte.parseByte(encryptionByteString));
        IAsymmetricConvertor publicKeyConvertor = Asymmetric.getPublicConvertor(encryption);
        String string = new String(response.getBody(), StandardCharsets.US_ASCII);
        AsymmetricKeyStorage keyStorage = publicKeyConvertor.toKeyStorage(string);
        clientKeyStorage.setKeyStorage(encryption, keyStorage);
        return encryption;
    }

    public void createAESKey() {
        AESKeyStorage aesKeyStorage = AESGenerator.getInstance().generateKeyStorage();
        clientKeyStorage.setAESKeyStorage(aesKeyStorage);
    }

    public void sendAESKey() throws KeyNotCreated, ReadingKeyException, EncryptionException, IOException {
        if (clientKeyStorage.getAESKeyStorage() == null) {
            throw new KeyNotCreated("AES");
        }

        if (clientKeyStorage.getKeyStorage(encryptionOfServer) == null) {
            throw new KeyNotCreated("RSA");
        }

        clientKeyStorage.getKeyStorage(encryptionOfServer);

        HeadersBuilder headersBuilder = new HeadersBuilder()
                .set(encryptionOfServer)
                .set(CLIENT2SERVER)
                .set(SYMKEY)
                .set("application/octet-stream")
                .set("encryption", "" + AES.asByte());

        byte[] aesKey = clientKeyStorage.getAESKeyStorage().getKey().getEncoded();

        RawMessage rawMessage = new RawMessage(headersBuilder, aesKey);

        sendMessage(rawMessage, encryptionOfServer);
    }

    public Encryption getKeys() throws EncryptionException, IOException, NoSuchEncryptionException, ReadingKeyException, CreatingKeyException, CannotUseEncryption, NoSuchAlgorithmException, DecryptionException, NoSuchReqHandler {
        encryptionOfServer = getPublicKeyFromServer();
        createAESKey();
        try {
            sendAESKey();
        } catch (KeyNotCreated | ReadingKeyException ignored) {
        }
        return encryptionOfServer;
    }

    public void disconnect() throws IOException {
        socket.close();
    }
}
