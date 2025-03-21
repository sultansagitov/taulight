package net.result.sandnode.chain.receiver;

import net.result.sandnode.serverclient.Session;
import net.result.sandnode.chain.Chain;

public abstract class ServerChain extends Chain {
    protected final Session session;

    public ServerChain(Session session) {
        super(session.io);
        this.session = session;
    }
}
