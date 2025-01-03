package net.result.taulight;

import net.result.sandnode.Agent;
import net.result.sandnode.exceptions.InputStreamException;
import net.result.sandnode.exceptions.OutputStreamException;
import net.result.sandnode.exceptions.WrongNodeUsedException;
import net.result.sandnode.messages.util.Connection;
import net.result.sandnode.server.SandnodeServer;
import net.result.sandnode.server.Session;
import net.result.sandnode.encryption.GlobalKeyStorage;
import net.result.sandnode.util.IOControl;
import net.result.sandnode.chain.server.ServerChainManager;
import org.jetbrains.annotations.NotNull;

import java.net.Socket;

public class TauAgent extends Agent {

    public TauAgent() {
        this(new GlobalKeyStorage());
    }

    @Override
    public @NotNull Session createSession(SandnodeServer server, Socket socket, Connection connection)
            throws WrongNodeUsedException, OutputStreamException, InputStreamException {

        ServerChainManager chainManager = new TauBSTServerChainManager();
        IOControl io = new IOControl(socket, connection, globalKeyStorage, chainManager);
        Session session = new Session(server, socket, chainManager, io);
        switch (connection) {
            case AGENT2HUB -> hubSessionList.add(session);
            case AGENT2AGENT -> agentSessionList.add(session);
            default -> throw new WrongNodeUsedException(connection);
        }

        return session;
    }

    public TauAgent(@NotNull GlobalKeyStorage globalKeyStorage) {
        super(globalKeyStorage);
    }

}
