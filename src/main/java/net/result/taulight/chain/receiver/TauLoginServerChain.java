package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.receiver.LoginServerChain;
import net.result.taulight.util.LoginUtil;

public class TauLoginServerChain extends LoginServerChain {
    @Override
    protected void onLogin() {
        LoginUtil.onLogin(session);
    }
}
