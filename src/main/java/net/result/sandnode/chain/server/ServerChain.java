package net.result.sandnode.chain.server;

import net.result.sandnode.server.Session;
import net.result.sandnode.chain.Chain;

public abstract class ServerChain extends Chain {
    protected final Session session;

    public ServerChain(Session session) {
        super(session.io);
        this.session = session;
    }
}
