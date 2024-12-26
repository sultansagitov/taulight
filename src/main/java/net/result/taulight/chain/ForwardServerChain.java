package net.result.taulight.chain;

import net.result.sandnode.chain.server.ServerChain;
import net.result.sandnode.exceptions.*;
import net.result.sandnode.server.Session;

public class ForwardServerChain extends ServerChain {
    public ForwardServerChain(Session session) {
        super(session);
    }

    @Override
    public boolean isChainStartAllowed() {
        return false;
    }

    @Override
    public void start() {
        throw new ImpossibleRuntimeException("This chain should no be started");
    }
}
