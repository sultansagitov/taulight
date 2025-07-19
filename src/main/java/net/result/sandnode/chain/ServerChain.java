package net.result.sandnode.chain;

import net.result.sandnode.serverclient.Session;

public abstract class ServerChain extends BaseChain {
    protected final Session session;

    public ServerChain(Session session) {
        super(session.io);
        this.session = session;
    }
}
