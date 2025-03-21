package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.receiver.LoginServerChain;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.util.LoginUtil;

public class TauLoginServerChain extends LoginServerChain {
    public TauLoginServerChain(Session session) {
        super(session);
    }

    @Override
    protected void onLogin() throws InterruptedException, UnprocessedMessagesException {
        LoginUtil.onLogin(session, this);
    }
}
