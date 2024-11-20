package net.result.sandnode.client;

import net.result.sandnode.Node;
import net.result.sandnode.exceptions.*;
import net.result.sandnode.exceptions.encryption.CannotUseEncryption;
import net.result.sandnode.exceptions.encryption.DecryptionException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.messages.EmptyMessage;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.Message;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.messages.util.Connection;
import net.result.sandnode.messages.util.HeadersBuilder;
import net.result.sandnode.messages.util.NodeType;
import net.result.sandnode.util.Endpoint;
import net.result.sandnode.util.encryption.Encryptions;
import net.result.sandnode.util.encryption.interfaces.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static net.result.sandnode.messages.util.MessageTypes.PUB;
import static net.result.sandnode.messages.util.MessageTypes.SYM;
import static net.result.sandnode.util.encryption.Encryption.NONE;

public class Client {
    private static final Logger LOGGER = LogManager.getLogger(Client.class);

    private final Node node;
    private final NodeType toNodeType;
    private final Endpoint endpoint;
    private IAsymmetricEncryption serverEncryption;
    private Socket socket;
    private OutputStream out;
    private InputStream in;
    private volatile boolean outBusy = false;
    private volatile boolean inBusy = false;

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
            LOGGER.info("Connecting to {}", endpoint.toString());
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
            @NotNull IEncryption headersEncryption
    ) throws IOException, ReadingKeyException, EncryptionException, UnexpectedSocketDisconnect,
            KeyStorageNotFoundException {
        if (isConnected()) {
            message.getHeadersBuilder().set(Connection.fromType(node.type(), toNodeType));
            byte[] byteArray = message.toByteArray(node.globalKeyStorage, headersEncryption);
            if (outBusy)
                LOGGER.info("Waiting for sending message in other thread by {} {}", headersEncryption.name(), message);
            while (outBusy) Thread.onSpinWait();
            outBusy = true;
            out.write(byteArray);
            out.flush();
            LOGGER.info("Message sent by {}: {}", headersEncryption.name(), message);
            outBusy = false;
        } else {
            throw new UnexpectedSocketDisconnect();
        }
    }

    public @Nullable IMessage receiveMessage() throws NoSuchEncryptionException, ReadingKeyException,
            DecryptionException, NoSuchReqHandler, KeyStorageNotFoundException, UnexpectedSocketDisconnect {
        if (isConnected()) {
            if (inBusy) LOGGER.info("Waiting for reading message in other thread");
            while (inBusy) Thread.onSpinWait();
            inBusy = true;
            Message.EncryptedMessage encrypted = Message.readMessage(in);
            inBusy = false;
            return Message.decryptMessage(node.globalKeyStorage, encrypted);
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
            UnexpectedSocketDisconnect, KeyStorageNotFoundException, CannotUseEncryption {
        HeadersBuilder headersBuilder = new HeadersBuilder().set(PUB);
        IMessage request = new EmptyMessage(headersBuilder);
        sendMessage(request, NONE);

        IMessage response = receiveMessage();
        assert response != null;
        String encryptionByteString = response.getHeaders().get("encryption");
        IAsymmetricEncryption encryptionServer = Encryptions.findAsymmetric(Byte.parseByte(encryptionByteString));
        IAsymmetricConvertor publicKeyConvertor = encryptionServer.publicKeyConvertor();
        String string = new String(response.getBody(), StandardCharsets.US_ASCII);
        IAsymmetricKeyStorage keyStorage = publicKeyConvertor.toKeyStorage(string);
        setServerKey(keyStorage);
    }

    public void sendSymmetricKey() throws KeyNotCreated, ReadingKeyException, EncryptionException, IOException,
            UnexpectedSocketDisconnect, KeyStorageNotFoundException, CannotUseEncryption {
        ISymmetricEncryption symmetricEncryption = node.config.getSymmetricKeyEncryption();
        ISymmetricKeyStorage keyStorage = (ISymmetricKeyStorage) symmetricEncryption.generator().generate();
        node.globalKeyStorage.set(keyStorage);

        if (!node.globalKeyStorage.has(getServerEncryption())) throw new KeyNotCreated(getServerEncryption());
        if (!node.globalKeyStorage.has(symmetricEncryption)) throw new KeyNotCreated(symmetricEncryption);

        node.globalKeyStorage.get(getServerEncryption());

        HeadersBuilder headersBuilder = new HeadersBuilder()
                .set(getServerEncryption())
                .set(SYM)
                .set("encryption", "" + symmetricEncryption.asByte());

        byte[] aesKey = symmetricEncryption.keyConvertor().toBytes(keyStorage);
        RawMessage rawMessage = new RawMessage(headersBuilder, aesKey);

        sendMessage(rawMessage, getServerEncryption());
    }

    public void disconnect() throws IOException {
        socket.close();
    }

    public IAsymmetricEncryption getServerEncryption() {
        return serverEncryption;
    }

    public void setServerKey(@NotNull IAsymmetricKeyStorage publicKey) {
        serverEncryption = publicKey.encryption();
        node.globalKeyStorage.set(publicKey);
    }
}
