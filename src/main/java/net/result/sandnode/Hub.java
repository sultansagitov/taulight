package net.result.sandnode;

import net.result.sandnode.config.IHubConfig;
import net.result.sandnode.exceptions.*;
import net.result.sandnode.exceptions.DecryptionException;
import net.result.sandnode.exceptions.EncryptionException;
import net.result.sandnode.exceptions.NoSuchEncryptionException;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.messages.types.GroupMessage;
import net.result.sandnode.messages.util.Connection;
import net.result.sandnode.messages.util.IMessageType;
import net.result.sandnode.messages.util.NodeType;
import net.result.sandnode.server.SandnodeServer;
import net.result.sandnode.server.Session;
import net.result.sandnode.encryption.GlobalKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.net.Socket;
import java.nio.BufferUnderflowException;
import java.util.List;
import java.util.Set;

import static net.result.sandnode.messages.util.MessageTypes.EXIT;
import static net.result.sandnode.messages.util.MessageTypes.GROUP;
import static net.result.sandnode.messages.util.NodeType.HUB;

public abstract class Hub extends Node {
    private static final Logger LOGGER = LogManager.getLogger(Hub.class);

    public Hub(@NotNull GlobalKeyStorage globalKeyStorage, @NotNull IHubConfig hubConfig) {
        super(globalKeyStorage, hubConfig);
    }

    @Override
    public @NotNull NodeType type() {
        return HUB;
    }

    @Override
    public @NotNull Session createSession(
            @NotNull SandnodeServer server,
            @NotNull Socket socket,
            @NotNull Connection connection
    ) throws WrongNodeUsedException, OutputStreamException, InputStreamException {
        Session session = new Session(server, connection, socket, globalKeyStorage);
        switch (connection) {
            case HUB2AGENT -> agentSessionList.add(session);
            case HUB2HUB -> hubSessionList.add(session);
            default -> throw new WrongNodeUsedException(connection);
        }

        return session;
    }

    @Override
    public void close() {
    }

    @Override
    public void initSession(
            @NotNull SandnodeServer server,
            @NotNull Connection connection,
            @NotNull Session session
    ) throws WrongNodeUsedException {
        List<Session> sessionList = switch (connection) {
            case HUB2HUB -> this.hubSessionList;
            case HUB2AGENT -> this.agentSessionList;
            default -> throw new WrongNodeUsedException(connection);
        };

        try {
            while (server.isRunning() && session.io.isConnected()) {
                RawMessage request;
                try {
                    request = session.io.receiveMessage();
                } catch (UnexpectedSocketDisconnectException e) {
                    if (server.isRunning() && session.io.isConnected()) throw e;
                    else break;
                }
                IMessageType type = request.getHeaders().getType();

                if (type == GROUP) {
                    try {
                        GroupMessage groupMessage = new GroupMessage(request);
                        Set<String> groupNames = groupMessage.getGroupNames();
                        server.groups.addToGroup(groupNames, session);
                    } catch (ExpectedMessageException e) {
                        throw new RuntimeException(e);
                    }
                    continue;
                } else if (type == EXIT) {
                    break;
                }
                onAgentMessage(request, session);
            }
        } catch (BufferUnderflowException | NoSuchEncryptionException | KeyStorageNotFoundException |
                 DecryptionException | EncryptionException | NoSuchMessageTypeException |
                 UnexpectedSocketDisconnectException |
                 MessageSerializationException | MessageWriteException | IllegalMessageLengthException e) {
            LOGGER.error("Unknown", e);
        }

        if (session.socket.isConnected()) {
            try {
                session.io.disconnect();
            } catch (SocketClosingException e) {
                LOGGER.error("Error while closing socket", e);
            }
        }

        sessionList.remove(session);
        LOGGER.info("Client disconnected");
    }

}
