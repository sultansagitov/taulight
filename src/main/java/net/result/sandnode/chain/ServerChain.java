package net.result.sandnode.chain;

import net.result.sandnode.serverclient.Session;

public abstract class ServerChain extends BaseChain {
    protected Session session;

    public void setSession(Session session) {
        setIO(session.io());
        this.session = session;
    }
}
