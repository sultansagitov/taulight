package net.result.taulight;

import net.result.sandnode.hubagent.Agent;

import net.result.sandnode.message.util.Connection;
import net.result.sandnode.serverclient.SandnodeServer;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.encryption.GlobalKeyStorage;
import org.jetbrains.annotations.NotNull;

import java.net.Socket;

public class TauAgent extends Agent {

    public TauAgent() {
        this(new GlobalKeyStorage());
    }

    @Override
    public @NotNull Session createSession(SandnodeServer server, Socket socket, Connection connection) {
        //TODO realize it when add agent as server
        return null;
    }

    public TauAgent(@NotNull GlobalKeyStorage globalKeyStorage) {
        super(globalKeyStorage);
    }

}
