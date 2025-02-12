package net.result.taulight.chain.server;

import net.result.sandnode.chain.server.LogPasswdServerChain;
import net.result.sandnode.serverclient.Session;

public class TauLogPasswdServerChain extends LogPasswdServerChain {
    public TauLogPasswdServerChain(Session session) {
        super(session);
    }

    @Override
    protected void onLogin() throws InterruptedException {
        LoginUtil.onLogin(session, this);
    }
}
