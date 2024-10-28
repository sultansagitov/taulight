package net.result.sandnode.server;

import net.result.sandnode.exceptions.FirstByteEOFException;
import net.result.sandnode.exceptions.NoSuchReqHandler;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.DecryptionException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.messages.RawMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static net.result.sandnode.messages.util.MessageType.EXT;

public class ClientHandler extends Thread {
    private static final Logger LOGGER = LogManager.getLogger(ClientHandler.class);
    private final List<Session> sessionList;
    private final Session session;

    public ClientHandler(
            @NotNull List<Session> sessionList,
            @NotNull Session session
    ) {
        setName("Server-thread %s".formatted(session.getIPString()));
        this.sessionList = sessionList;
        this.session = session;
    }

    @Override
    public void run() {
        try {
            while (true) {
                RawMessage request = session.receiveMessage();
                if (request.getHeaders().getType() == EXT) break;
            }
        } catch (FirstByteEOFException ignored) {
        } catch (IOException | BufferUnderflowException e) {
            LOGGER.error("I/O Error", e);
        } catch (NoSuchEncryptionException | ReadingKeyException | NoSuchAlgorithmException | DecryptionException |
                 NoSuchReqHandler e) {
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
