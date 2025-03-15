package net.result.taulight.chain.server;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.server.LogPasswdServerChain;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.serverclient.Session;

public class TauLogPasswdServerChain extends LogPasswdServerChain implements ReceiverChain {
    public TauLogPasswdServerChain(Session session) {
        super(session);
    }

    @Override
    protected void onLogin() throws InterruptedException, UnprocessedMessagesException, UnauthorizedException {
        LoginUtil.onLogin(session, this);
    }
}
