package net.result.sandnode;

import net.result.sandnode.exceptions.*;
import net.result.sandnode.messages.util.Connection;
import net.result.sandnode.messages.util.NodeType;
import net.result.sandnode.server.SandnodeServer;
import net.result.sandnode.server.Session;
import net.result.sandnode.encryption.GlobalKeyStorage;
import org.jetbrains.annotations.NotNull;

import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Node {
    public final GlobalKeyStorage globalKeyStorage;
    public final Set<Session> agentSessionList;
    public final Set<Session> hubSessionList;

    public Node(@NotNull GlobalKeyStorage globalKeyStorage) {
        this.globalKeyStorage = globalKeyStorage;
        hubSessionList = ConcurrentHashMap.newKeySet();
        agentSessionList = ConcurrentHashMap.newKeySet();
    }

    public Node() {
        this(new GlobalKeyStorage());
    }

    public abstract @NotNull Session createSession(SandnodeServer server, Socket socket, Connection connection)
            throws WrongNodeUsedException, OutputStreamException, InputStreamException;

    protected void addAsHub(Session session) {
        hubSessionList.add(session);
    }

    protected void addAsAgent(Session session) {
        agentSessionList.add(session);
    }

    public abstract @NotNull NodeType type();

    public abstract void close();
}
