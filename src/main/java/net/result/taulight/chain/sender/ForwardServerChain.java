package net.result.taulight.chain.sender;

import net.result.sandnode.chain.receiver.ServerChain;
import net.result.sandnode.serverclient.Session;

public class ForwardServerChain extends ServerChain {
    public ForwardServerChain(Session session) {
        super(session);
    }
}

