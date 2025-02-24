package net.result.taulight.hubagent;

import net.result.sandnode.hubagent.Agent;

import net.result.sandnode.message.util.Connection;
import net.result.sandnode.serverclient.SandnodeServer;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.encryption.KeyStorageRegistry;
import org.jetbrains.annotations.NotNull;

import java.net.Socket;

public class TauAgent extends Agent {

    public TauAgent() {
        this(new KeyStorageRegistry());
    }

    //TODO:
    // remove SuppressWarnings
    // realize it when add agent as server
    @SuppressWarnings("DataFlowIssue")
    @Override
    public @NotNull Session createSession(SandnodeServer server, Socket socket, Connection connection) {
        return null;
    }

    public TauAgent(@NotNull KeyStorageRegistry keyStorageRegistry) {
        super(keyStorageRegistry);
    }

}
