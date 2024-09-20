package net.result.sandnode.server;

import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.DecryptionException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.Message;
import net.result.sandnode.messages.util.MessageType;
import net.result.sandnode.server.handlers.HandlersFactory;
import net.result.sandnode.server.handlers.IProtocolHandler;
import net.result.sandnode.server.handlers.MessageHandler;
import net.result.sandnode.util.encryption.Encryption;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.BufferUnderflowException;
import java.security.NoSuchAlgorithmException;

class ClientHandler extends Thread {
    private static final Logger LOGGER = LogManager.getLogger(ClientHandler.class);
    private final Socket socket;
    protected final GlobalKeyStorage globalKeyStorage;
    private final InputStream in;
    private final OutputStream out;

    public ClientHandler(@NotNull Socket socket, @NotNull GlobalKeyStorage globalKeyStorage) throws IOException {
        this.socket = socket;
        this.setName("%s:%d".formatted(socket.getInetAddress().getHostAddress(), socket.getPort()));
        this.globalKeyStorage = globalKeyStorage;

        in = socket.getInputStream();
        out = socket.getOutputStream();
    }

    @Override
    public void run() {
        LOGGER.info("Client connected! {}:{}", socket.getInetAddress().getHostAddress(), socket.getPort());
        while (true) {
            try {
                IMessage request = Message.fromInput(in, globalKeyStorage);

                IProtocolHandler requestHandler = HandlersFactory.getHandler(request.getHeaders().getType());
                @Nullable IMessage response = requestHandler.getResponse(request);

                if (response == null) {
                    break;
                }

                out.write(response.toByteArray(Encryption.NO, globalKeyStorage));
                LOGGER.info("Message was sent: {}", response);
            } catch (IOException | BufferUnderflowException e) {
                LOGGER.error("I/O Error", e);
                throw new RuntimeException(e);
            } catch (NoSuchEncryptionException | ReadingKeyException | NoSuchAlgorithmException |
                     DecryptionException | EncryptionException e) {
                LOGGER.error("Unknown", e);
                throw new RuntimeException(e);
            }

            LOGGER.info("Client disconnected");
        }
    }
}
