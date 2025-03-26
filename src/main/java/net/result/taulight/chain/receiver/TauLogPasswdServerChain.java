package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.receiver.LogPasswdServerChain;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.util.LoginUtil;

public class TauLogPasswdServerChain extends LogPasswdServerChain implements ReceiverChain {
    public TauLogPasswdServerChain(Session session) {
        super(session);
    }

    @Override
    protected void onLogin() throws Exception {
        LoginUtil.onLogin(session, this);
    }
}
