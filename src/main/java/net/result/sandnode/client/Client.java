package net.result.sandnode.client;

import net.result.sandnode.exceptions.CreatingKeyException;
import net.result.sandnode.exceptions.FirstByteEOFException;
import net.result.sandnode.exceptions.NoSuchReqHandler;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.CannotUseEncryption;
import net.result.sandnode.exceptions.encryption.DecryptionException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.messages.HeadersBuilder;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.Message;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.util.encryption.Encryption;
import net.result.sandnode.util.encryption.EncryptionFactory;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import net.result.sandnode.util.encryption.asymmetric.AsymmetricEncryptionFactory;
import net.result.sandnode.util.encryption.asymmetric.AsymmetricKeyStorage;
import net.result.sandnode.util.encryption.asymmetric.interfaces.IAsymmetricConvertor;
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
import static net.result.sandnode.util.encryption.Encryption.NO;

public class Client {
    private static final Logger LOGGER = LogManager.getLogger(Client.class);

    private final String serverAddress;
    private final int port;
    private Socket socket;
    private OutputStream out;
    private InputStream in;
    private final @NotNull GlobalKeyStorage globalKeyStorage;

    public Client(@NotNull String serverAddress, int port, @NotNull GlobalKeyStorage globalKeyStorage) {
        this.serverAddress = serverAddress;
        this.port = port;
        this.globalKeyStorage = globalKeyStorage;
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

    public void sendMessage(@NotNull Encryption headersEncryption, @NotNull IMessage message) throws IOException, ReadingKeyException, EncryptionException {
        if (isConnected()) {
            out.write(message.toByteArray(globalKeyStorage, headersEncryption));
            out.flush();
            LOGGER.info("Message sent: {}", message);
        }
    }

    public @Nullable IMessage receiveMessage() throws NoSuchEncryptionException, ReadingKeyException, NoSuchAlgorithmException, DecryptionException, NoSuchReqHandler {
        if (isConnected()) {
            try {
                RawMessage response = Message.fromInput(in, globalKeyStorage);
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
        sendMessage(NO, request);

        IMessage response = receiveMessage();
        String encryptionString = response.getHeaders().get("Encryption");
        LOGGER.debug("encr: {}", encryptionString);
        Encryption encryption = EncryptionFactory.getEncryption(encryptionString);
        LOGGER.debug("encr: {}", encryption);
        IAsymmetricConvertor publicKeyConvertor = AsymmetricEncryptionFactory.getPublicConvertor(encryption);
        String string = new String(response.getBody(), StandardCharsets.US_ASCII);
        AsymmetricKeyStorage keyStorage = publicKeyConvertor.toKeyStorage(string);
        EncryptionFactory.setKeyStorage(globalKeyStorage, encryption, keyStorage);
        return encryption;
    }

    public void disconnect() throws IOException {
        socket.close();
    }
}
