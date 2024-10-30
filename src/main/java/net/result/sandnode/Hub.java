package net.result.sandnode;

import net.result.sandnode.exceptions.FirstByteEOFException;
import net.result.sandnode.exceptions.NoSuchReqHandler;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.WrongNodeUsed;
import net.result.sandnode.exceptions.encryption.DecryptionException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.messages.util.Connection;
import net.result.sandnode.messages.util.NodeType;
import net.result.sandnode.server.Session;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.Socket;
import java.nio.BufferUnderflowException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static net.result.sandnode.messages.util.MessageType.EXT;
import static net.result.sandnode.messages.util.NodeType.HUB;

public abstract class Hub extends Node {
    private static final Logger LOGGER = LogManager.getLogger(Hub.class);

    public Hub(@NotNull GlobalKeyStorage globalKeyStorage) {
        super(globalKeyStorage);
    }

    public Hub() {
        super();
    }

    @Override
    public @NotNull NodeType type() {
        return HUB;
    }

    @Override
    public @NotNull Session createSession(
            @NotNull Connection opposite,
            @NotNull Socket socket
    ) throws IOException, WrongNodeUsed {
        Session session = new Session(this, socket, globalKeyStorage);
        switch (opposite) {
            case HUB2USER -> userSessionList.add(session);
            case HUB2HUB -> hubSessionList.add(session);
            default -> throw new WrongNodeUsed(opposite);
        }

        return session;
    }

    @Override
    public void initSession(@NotNull Connection opposite, @NotNull Session session) throws WrongNodeUsed {
        List<Session> sessionList = switch (opposite) {
            case HUB2HUB -> this.hubSessionList;
            case HUB2USER -> this.userSessionList;
            default -> throw new WrongNodeUsed(opposite);
        };

        try {
            while (true) {
                RawMessage request = session.receiveMessage();
                if (request.getHeaders().getType() == EXT) break;
                onUserMessage(request, sessionList, session);
            }
        } catch (FirstByteEOFException ignored) {
        } catch (IOException | BufferUnderflowException e) {
            LOGGER.error("I/O Error", e);
        } catch (NoSuchEncryptionException | ReadingKeyException | NoSuchAlgorithmException | DecryptionException |
                 EncryptionException | NoSuchReqHandler e) {
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

    @Override
    public void close() {
    }

}
