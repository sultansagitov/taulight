package net.result.sandnode.hubagent;

import net.result.sandnode.exception.*;
import net.result.sandnode.message.util.Connection;
import net.result.sandnode.message.util.NodeType;
import net.result.sandnode.serverclient.SandnodeServer;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.encryption.GlobalKeyStorage;
import org.jetbrains.annotations.NotNull;

import java.net.Socket;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Node {
    public final GlobalKeyStorage globalKeyStorage;
    public final Collection<Session> agentSessionList;
    public final Collection<Session> hubSessionList;

    public Node(@NotNull GlobalKeyStorage globalKeyStorage) {
        this.globalKeyStorage = globalKeyStorage;
        hubSessionList = ConcurrentHashMap.newKeySet();
        agentSessionList = ConcurrentHashMap.newKeySet();
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

    public void close() {}
}
