package net.result.sandnode.server;

import net.result.sandnode.exceptions.FirstByteEOFException;
import net.result.sandnode.exceptions.NoSuchReqHandler;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.DecryptionException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.server.commands.ICommand;
import net.result.sandnode.server.handlers.IProtocolHandler;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.Socket;
import java.nio.BufferUnderflowException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

class ClientHandler extends Thread {
    private static final Logger LOGGER = LogManager.getLogger(ClientHandler.class);
    private final GlobalKeyStorage globalKeyStorage;
    private final Socket socket;
    private final List<Session> sessionList;
    private final Session session;

    public ClientHandler(
            @NotNull GlobalKeyStorage globalKeyStorage,
            @NotNull List<Session> sessionList,
            @NotNull Session session
    ) {
        setName("Server-thread %s:%d".formatted(session.socket.getInetAddress().getHostAddress(), session.socket.getPort()));
        this.socket = session.socket;
        this.globalKeyStorage = globalKeyStorage;
        this.sessionList = sessionList;
        this.session = session;
    }

    @Override
    public void run() {
        LOGGER.info("Client connected!");
        try {
            while (true) {
                RawMessage request = session.receiveMessage();
                LOGGER.info("Requested {}", request);

                @Nullable IProtocolHandler requestHandler = request.getType().handler();
                @Nullable ICommand command =
                        requestHandler != null
                                ? requestHandler.getCommand(request, sessionList, session, globalKeyStorage)
                                : null;

                if (command == null) break;

                command.execute(sessionList, session, globalKeyStorage);
            }
        } catch (FirstByteEOFException ignored) {
        } catch (IOException | BufferUnderflowException e) {
            LOGGER.error("I/O Error", e);
        } catch (NoSuchEncryptionException | ReadingKeyException | NoSuchAlgorithmException |
                 DecryptionException | EncryptionException | NoSuchReqHandler e) {
            LOGGER.error("Unknown", e);
        }

        try {
            session.close();
        } catch (IOException e) {
            LOGGER.error("Error while closing socket", e);
        }
        sessionList.remove(session);
        LOGGER.info("Client disconnected");
    }
}
