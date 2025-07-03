package net.result.sandnode.hubagent;

import net.result.sandnode.chain.ServerChainManager;
import net.result.sandnode.encryption.KeyStorageRegistry;
import net.result.sandnode.exception.InputStreamException;
import net.result.sandnode.exception.OutputStreamException;
import net.result.sandnode.exception.WrongNodeUsedException;
import net.result.sandnode.message.util.Connection;
import net.result.sandnode.message.util.NodeType;
import net.result.sandnode.serverclient.SandnodeServer;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.util.IOController;
import org.jetbrains.annotations.NotNull;

import java.net.Socket;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Node {
    public final KeyStorageRegistry keyStorageRegistry;
    private final Collection<Session> agentSessions;
    private final Collection<Session> hubSessions;

    public Node(@NotNull KeyStorageRegistry keyStorageRegistry) {
        this.keyStorageRegistry = keyStorageRegistry;
        hubSessions = ConcurrentHashMap.newKeySet();
        agentSessions = ConcurrentHashMap.newKeySet();
    }

    protected abstract @NotNull ServerChainManager createChainManager();

    public @NotNull Session createSession(SandnodeServer server, Socket socket, Connection connection)
            throws WrongNodeUsedException, OutputStreamException, InputStreamException {
        if (connection.getFrom() != type()) throw new WrongNodeUsedException(connection);

        ServerChainManager chainManager = createChainManager();
        IOController io = new IOController(socket, connection, this.keyStorageRegistry, chainManager);
        Session session = new Session(server, io);
        switch (connection.getTo()) {
            case AGENT -> addAsAgent(session);
            case HUB -> addAsHub(session);
        }

        return session;
    }

    protected void addAsAgent(Session session) {
        agentSessions.add(session);
    }

    protected void addAsHub(Session session) {
        hubSessions.add(session);
    }

    public void removeSession(Session session) {
        hubSessions.remove(session);
        agentSessions.remove(session);
    }

    public Collection<Session> getAgents() {
        return agentSessions;
    }

    public Collection<Session> getHubs() {
        return hubSessions;
    }

    public abstract @NotNull NodeType type();

    @SuppressWarnings("EmptyMethod")
    public void close() {}

    @Override
    public String toString() {
        return "<%s %s>".formatted(getClass().getSimpleName(), keyStorageRegistry);
    }
}
