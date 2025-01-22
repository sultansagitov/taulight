package net.result.sandnode.hubagent;

import net.result.sandnode.exception.InputStreamException;
import net.result.sandnode.exception.OutputStreamException;
import net.result.sandnode.exception.WrongNodeUsedException;
import net.result.sandnode.message.util.Connection;
import net.result.sandnode.message.util.NodeType;
import net.result.sandnode.encryption.GlobalKeyStorage;
import net.result.sandnode.serverclient.SandnodeServer;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.util.IOControl;
import net.result.sandnode.chain.server.ServerChainManager;
import org.jetbrains.annotations.NotNull;

import java.net.Socket;

import static net.result.sandnode.message.util.NodeType.HUB;

public abstract class Hub extends Node {
    public Hub(@NotNull GlobalKeyStorage globalKeyStorage) {
        super(globalKeyStorage);
    }

    @Override
    public @NotNull NodeType type() {
        return HUB;
    }

    @Override
    public @NotNull Session createSession(SandnodeServer server, Socket socket, Connection connection)
            throws WrongNodeUsedException, OutputStreamException, InputStreamException {
        ServerChainManager chainManager = createChainManager();
        IOControl io = new IOControl(socket, connection, this.globalKeyStorage, chainManager);
        Session session = new Session(server, socket, chainManager, io);
        switch (connection) {
            case HUB2AGENT -> addAsAgent(session);
            case HUB2HUB -> addAsHub(session);
            default -> throw new WrongNodeUsedException(connection);
        }

        return session;
    }

    protected abstract ServerChainManager createChainManager();
}
