package net.result.sandnode.util;

import net.result.sandnode.Node;
import net.result.sandnode.encryption.GlobalKeyStorage;
import net.result.sandnode.encryption.interfaces.*;
import net.result.sandnode.exceptions.*;
import net.result.sandnode.messages.EncryptedMessage;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.Message;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.messages.util.Connection;
import net.result.sandnode.messages.util.NodeType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import static net.result.sandnode.encryption.Encryption.NONE;

public class IOControl {
    private static final Logger LOGGER = LogManager.getLogger(IOControl.class);

    private final InputStream in;
    private final OutputStream out;
    private final Socket socket;
    private final Connection connection;
    private final GlobalKeyStorage globalKeyStorage;
    private IEncryption serverMainEncryption = NONE;
    private IEncryption symKeyEncryption = NONE;

    public IOControl(Socket socket, Node node, NodeType nodeType, GlobalKeyStorage globalKeyStorage)
            throws InputStreamException, OutputStreamException {
        this(socket, Connection.fromType(node.type(), nodeType), globalKeyStorage);
    }

    public IOControl(Socket socket, Connection connection, GlobalKeyStorage globalKeyStorage)
            throws InputStreamException, OutputStreamException {
        this.in = StreamReader.inputStream(socket);
        this.out = StreamReader.outputStream(socket);
        this.socket = socket;
        this.connection = connection;
        this.globalKeyStorage = globalKeyStorage;
    }

    public static @NotNull String getIP(@NotNull Socket socket) {
        return String.format("%s:%d", socket.getInetAddress().getHostAddress(), socket.getPort());
    }

    public @NotNull Connection getConnection() {
        return connection;
    }

    public @NotNull IEncryption getServerMainEncryption() {
        return serverMainEncryption;
    }

    public @NotNull IEncryption getSymKeyEncryption() {
        return symKeyEncryption;
    }

    public synchronized boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    public void setMainKey(@NotNull IAsymmetricKeyStorage publicKey) {
        serverMainEncryption = publicKey.encryption();
        globalKeyStorage.set(publicKey);
    }

    public void setSymmetricKey(@NotNull ISymmetricKeyStorage symmetricKeyStorage) {
        symKeyEncryption = symmetricKeyStorage.encryption();
        globalKeyStorage.set(symmetricKeyStorage);
    }

    private @NotNull IEncryption getServerEncryption() {
        return getSymKeyEncryption() != NONE ? getSymKeyEncryption() : getServerMainEncryption();
    }

    public void sendMessage(@NotNull IMessage message) throws EncryptionException, UnexpectedSocketDisconnectException,
            KeyStorageNotFoundException, MessageSerializationException, MessageWriteException {
        sendMessage(message, getServerEncryption());
    }

    public void sendMessage(@NotNull IMessage message, @NotNull IEncryption headersEncryption)
            throws EncryptionException, UnexpectedSocketDisconnectException, KeyStorageNotFoundException,
            MessageWriteException, MessageSerializationException {
        if (isConnected()) {
            message.getHeaders().set(connection);
            byte[] byteArray;
            try {
                byteArray = message.toByteArray(globalKeyStorage, headersEncryption);
            } catch (IllegalMessageLengthException e) {
                throw new MessageSerializationException("Failed to serialize message.", e);
            }
            synchronized (out) {
                try {
                    out.write(byteArray);
                    out.flush();
                } catch (IOException e) {
                    throw new MessageWriteException("Failed to write message to output.", e);
                }
            }

            LOGGER.info("Message sent by {}: {}", headersEncryption.name(), message);

        } else {
            throw new UnexpectedSocketDisconnectException("unknown");
        }
    }

    public @NotNull RawMessage receiveMessage() throws NoSuchEncryptionException, DecryptionException,
            NoSuchMessageTypeException, KeyStorageNotFoundException, UnexpectedSocketDisconnectException {
        EncryptedMessage encrypted;
        synchronized (in) {
            encrypted = EncryptedMessage.readMessage(in);
        }
        return Message.decryptMessage(globalKeyStorage, encrypted);
    }

    public synchronized void disconnect() throws SocketClosingException {
        LOGGER.info("Disconnecting from {}", getIP(socket));
        try {
            socket.close();
        } catch (IOException e) {
            throw new SocketClosingException("Cannot close socket", e);
        }
    }

}
