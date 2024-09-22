package net.result.sandnode.client;

import net.result.sandnode.exceptions.NoSuchReqHandler;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.DecryptionException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.Message;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.util.encryption.Encryption;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

public class Client {
    private static final Logger LOGGER = LogManager.getLogger(Client.class);

    private final String serverAddress;
    private final int port;
    private Socket socket;
    private Encryption headersEncryption;
    private OutputStream out;
    private InputStream in;
    private final @NotNull GlobalKeyStorage globalKeyStorage;

    public Client(@NotNull String serverAddress, int port, @NotNull GlobalKeyStorage globalKeyStorage) {
        this.serverAddress = serverAddress;
        this.port = port;
        this.globalKeyStorage = globalKeyStorage;
    }

    public void connect(@NotNull Encryption headersEncryption) {
        this.headersEncryption = headersEncryption;

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

    public void sendMessage(@NotNull IMessage message) throws IOException, ReadingKeyException, EncryptionException {
        if (isConnected()) {
            out.write(message.toByteArray(headersEncryption, globalKeyStorage));
            out.flush();
            LOGGER.info("Message sent: {}", message);
        }
    }

    public IMessage receiveMessage() throws NoSuchEncryptionException, ReadingKeyException, NoSuchAlgorithmException, DecryptionException, NoSuchReqHandler {
        if (isConnected()) {
            try {
                RawMessage response = Message.fromInput(in, globalKeyStorage);
                LOGGER.info("Received response: {}", response);
                return response;
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

    private boolean isConnected() {
        return socket != null && !socket.isClosed();
    }
}
