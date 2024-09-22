package net.result.sandnode.server;

import net.result.sandnode.exceptions.NoSuchReqHandler;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.DecryptionException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.messages.Message;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.server.commands.ICommand;
import net.result.sandnode.server.handlers.HandlersFactory;
import net.result.sandnode.server.handlers.IProtocolHandler;
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
import java.util.List;

class ClientHandler extends Thread {
    private static final Logger LOGGER = LogManager.getLogger(ClientHandler.class);
    private final GlobalKeyStorage globalKeyStorage;
    private final Socket socket;
    private final InputStream in;
    private final OutputStream out;
    private final List<Session> sessionList;
    private final Session session;

    public ClientHandler(
            @NotNull Socket socket,
            @NotNull GlobalKeyStorage globalKeyStorage,
            @NotNull List<Session> sessionList,
            @NotNull Session session
    ) throws IOException {
        setName("%s:%d".formatted(socket.getInetAddress().getHostAddress(), socket.getPort()));
        this.socket = socket;
        this.globalKeyStorage = globalKeyStorage;
        this.sessionList = sessionList;
        this.session = session;

        in = socket.getInputStream();
        out = socket.getOutputStream();
    }

    @Override
    public void run() {
        LOGGER.info("Client connected! {}:{}", socket.getInetAddress().getHostAddress(), socket.getPort());
        while (true) {
            try {
                RawMessage request = Message.fromInput(in, globalKeyStorage);
                LOGGER.debug("request: {}", request);

                IProtocolHandler requestHandler = HandlersFactory.getHandler(request.getType());
                @Nullable ICommand command = requestHandler.getCommand(request, sessionList, session, globalKeyStorage);
                LOGGER.debug("command: {}", command);

                if (command == null) {
                    sessionList.remove(session);
                    session.socket.close();
                    break;
                }

                command.execute(sessionList, session, globalKeyStorage);

            } catch (IOException | BufferUnderflowException e) {
                LOGGER.error("I/O Error", e);
            } catch (NoSuchEncryptionException | ReadingKeyException | NoSuchAlgorithmException |
                     DecryptionException | EncryptionException | NoSuchReqHandler e) {
                LOGGER.error("Unknown", e);
            }
        }

        try {
            session.socket.close();
        } catch (IOException ignored) {
        }
        sessionList.remove(session);
        LOGGER.info("Client disconnected");
    }
}
