package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.receiver.LogPasswdServerChain;
import net.result.taulight.util.LoginUtil;

public class TauLogPasswdServerChain extends LogPasswdServerChain implements ReceiverChain {
    @Override
    protected void onLogin() {
        LoginUtil.onLogin(session);
    }
}
