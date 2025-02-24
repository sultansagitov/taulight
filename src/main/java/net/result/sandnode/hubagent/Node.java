package net.result.sandnode.hubagent;

import net.result.sandnode.exception.*;
import net.result.sandnode.message.util.Connection;
import net.result.sandnode.message.util.NodeType;
import net.result.sandnode.serverclient.SandnodeServer;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.encryption.KeyStorageRegistry;
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

    public abstract @NotNull Session createSession(SandnodeServer server, Socket socket, Connection connection)
            throws WrongNodeUsedException, OutputStreamException, InputStreamException;

    protected void addAsAgent(Session session) {
        agentSessions.add(session);
    }

    protected void addAsHub(Session session) {
        hubSessions.add(session);
    }

    public void removeAgent(Session session) {
        agentSessions.remove(session);
    }

    public void removeHub(Session session) {
        hubSessions.remove(session);
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
}
