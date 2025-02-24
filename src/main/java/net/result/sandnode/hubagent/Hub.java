package net.result.sandnode.hubagent;

import net.result.sandnode.exception.InputStreamException;
import net.result.sandnode.exception.OutputStreamException;
import net.result.sandnode.exception.WrongNodeUsedException;
import net.result.sandnode.message.util.Connection;
import net.result.sandnode.message.util.NodeType;
import net.result.sandnode.encryption.KeyStorageRegistry;
import net.result.sandnode.serverclient.SandnodeServer;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.util.IOController;
import net.result.sandnode.chain.server.ServerChainManager;
import org.jetbrains.annotations.NotNull;

import java.net.Socket;

public abstract class Hub extends Node {
    public Hub(@NotNull KeyStorageRegistry keyStorageRegistry) {
        super(keyStorageRegistry);
    }

    @Override
    public @NotNull NodeType type() {
        return NodeType.HUB;
    }

    @Override
    public @NotNull Session createSession(SandnodeServer server, Socket socket, Connection connection)
            throws WrongNodeUsedException, OutputStreamException, InputStreamException {
        ServerChainManager chainManager = createChainManager();
        IOController io = new IOController(socket, connection, this.keyStorageRegistry, chainManager);
        Session session = new Session(server, io);
        switch (connection) {
            case HUB2AGENT -> addAsAgent(session);
            case HUB2HUB -> addAsHub(session);
            default -> throw new WrongNodeUsedException(connection);
        }

        return session;
    }

    protected abstract ServerChainManager createChainManager();
}
