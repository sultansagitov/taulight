package net.result.sandnode.client;

import net.result.sandnode.Node;
import net.result.sandnode.exceptions.*;
import net.result.sandnode.exceptions.encryption.CannotUseEncryption;
import net.result.sandnode.exceptions.encryption.DecryptionException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.messages.HeadersBuilder;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.Message;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.messages.util.Connection;
import net.result.sandnode.messages.util.NodeType;
import net.result.sandnode.util.Endpoint;
import net.result.sandnode.util.encryption.Encryption;
import net.result.sandnode.util.encryption.asymmetric.Asymmetric;
import net.result.sandnode.util.encryption.asymmetric.AsymmetricKeyStorage;
import net.result.sandnode.util.encryption.asymmetric.interfaces.IAsymmetricConvertor;
import net.result.sandnode.util.encryption.core.interfaces.IKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static net.result.sandnode.messages.util.Connection.USER2HUB;
import static net.result.sandnode.messages.util.MessageType.PUB;
import static net.result.sandnode.messages.util.MessageType.SYM;
import static net.result.sandnode.util.encryption.Encryption.NONE;

public class Client {
    private static final Logger LOGGER = LogManager.getLogger(Client.class);

    private final Node node;
    private final NodeType toNodeType;
    private final Endpoint endpoint;
    public Encryption encryptionOfServer;
    private Socket socket;
    private OutputStream out;
    private InputStream in;

    public Client(
            @NotNull Endpoint endpoint,
            @NotNull Node node,
            @NotNull NodeType toNodeType
    ) {
        this.endpoint = endpoint;
        this.node = node;
        this.toNodeType = toNodeType;
    }

    public void connect() {
        try {
            LOGGER.info("Connecting to {}", endpoint);
            socket = new Socket(endpoint.host, endpoint.port);

            in = socket.getInputStream();
            out = socket.getOutputStream();
            LOGGER.info("Connection established.");
        } catch (IOException e) {
            LOGGER.error("Error connecting to server", e);
            close();
        }
    }

    public void sendMessage(
            @NotNull IMessage message,
            @NotNull Encryption headersEncryption
    ) throws IOException, ReadingKeyException, EncryptionException {
        if (isConnected()) {
            message.getHeadersBuilder().set(Connection.fromType(node.type(), toNodeType));
            out.write(message.toByteArray(node.globalKeyStorage, headersEncryption));
            out.flush();
            LOGGER.info("Message sent: {}", message);
        }
    }

    public @Nullable IMessage receiveMessage() throws NoSuchEncryptionException, ReadingKeyException,
            DecryptionException, NoSuchReqHandler {
        if (isConnected()) {
            try {
                RawMessage response = Message.fromInput(in, node.globalKeyStorage);
                LOGGER.info("Received response: {}", response);
                return response;
            } catch (FirstByteEOFException ignored) {
            } catch (EOFException e) {
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

    public void getPublicKeyFromServer() throws ReadingKeyException, EncryptionException, IOException,
            NoSuchEncryptionException, DecryptionException, NoSuchReqHandler, CreatingKeyException,
            CannotUseEncryption {
        HeadersBuilder headersBuilder = new HeadersBuilder().set(USER2HUB).set(PUB);
        RawMessage request = new RawMessage(headersBuilder);
        request.setBody(new byte[0]);
        sendMessage(request, NONE);

        IMessage response = receiveMessage();
        assert response != null;
        String encryptionByteString = response.getHeaders().get("encryption");
        encryptionOfServer = Encryption.fromByte(Byte.parseByte(encryptionByteString));
        IAsymmetricConvertor publicKeyConvertor = Asymmetric.getPublicConvertor(encryptionOfServer);
        String string = new String(response.getBody(), StandardCharsets.US_ASCII);
        AsymmetricKeyStorage keyStorage = publicKeyConvertor.toKeyStorage(string);
        node.globalKeyStorage.set(keyStorage);
    }

    public void sendSymmetricKey() throws IOException, EncryptionException {
        Encryption symmetricEncryption = node.config.getSymmetricKeyEncryption();
        IKeyStorage keyStorage = symmetricEncryption.generator().generateKeyStorage();
        node.globalKeyStorage.set(keyStorage);

        try {
            if (!node.globalKeyStorage.has(encryptionOfServer)) throw new KeyNotCreated(encryptionOfServer);
            if (!node.globalKeyStorage.has(symmetricEncryption)) throw new KeyNotCreated(symmetricEncryption);

            node.globalKeyStorage.get(encryptionOfServer);

            HeadersBuilder headersBuilder = new HeadersBuilder()
                    .set(encryptionOfServer)
                    .set(USER2HUB)
                    .set(SYM)
                    .set("application/octet-stream")
                    .set("encryption", "" + symmetricEncryption.asByte());

            byte[] aesKey = node.globalKeyStorage.getSymmetric(symmetricEncryption).getKey().getEncoded();

            RawMessage rawMessage = new RawMessage(headersBuilder, aesKey);

            sendMessage(rawMessage, encryptionOfServer);
        } catch (KeyNotCreated | ReadingKeyException ignored) {
        }
    }

    public void disconnect() throws IOException {
        socket.close();
    }
}
