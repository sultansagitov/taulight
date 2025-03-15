package net.result.taulight.chain.server;

import net.result.sandnode.chain.server.LoginServerChain;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.serverclient.Session;

public class TauLoginServerChain extends LoginServerChain {
    public TauLoginServerChain(Session session) {
        super(session);
    }

    @Override
    protected void onLogin() throws InterruptedException, UnprocessedMessagesException, UnauthorizedException {
        LoginUtil.onLogin(session, this);
    }
}
