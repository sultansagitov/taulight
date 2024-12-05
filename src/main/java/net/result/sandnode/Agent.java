package net.result.sandnode;

import net.result.sandnode.config.IAgentConfig;
import net.result.sandnode.exceptions.*;
import net.result.sandnode.exceptions.FSException;
import net.result.sandnode.messages.util.Connection;
import net.result.sandnode.messages.util.NodeType;
import net.result.sandnode.server.SandnodeServer;
import net.result.sandnode.server.Session;
import net.result.sandnode.encryption.GlobalKeyStorage;
import org.jetbrains.annotations.NotNull;

import java.net.Socket;

import static net.result.sandnode.messages.util.NodeType.AGENT;

public abstract class Agent extends Node {
    public Agent(@NotNull GlobalKeyStorage globalKeyStorage, @NotNull IAgentConfig agentConfig) {
        super(globalKeyStorage, agentConfig);
    }

    public Agent() throws NoSuchEncryptionException, ConfigurationException, CannotUseEncryption, FSException {
        super();
    }

    @Override
    public @NotNull NodeType type() {
        return AGENT;
    }

    @Override
    public @NotNull Session createSession(
            @NotNull SandnodeServer server,
            @NotNull Socket socket, @NotNull Connection connection
    ) throws WrongNodeUsedException, OutputStreamException, InputStreamException {
        Session session = new Session(server, connection, socket, globalKeyStorage);
        switch (connection) {
            case AGENT2HUB -> hubSessionList.add(session);
            case AGENT2AGENT -> agentSessionList.add(session);
            default -> throw new WrongNodeUsedException(connection);
        }

        return session;
    }

    @Override
    public void initSession(@NotNull SandnodeServer server, @NotNull Connection opposite, @NotNull Session session) {
    }

    @Override
    public void close() {
    }

    public String memberID() {
        return "default-member-id";
    }
}
