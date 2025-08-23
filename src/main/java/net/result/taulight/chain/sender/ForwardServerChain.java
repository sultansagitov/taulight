package net.result.taulight.chain.sender;

import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.message.types.ForwardResponse;

public class ForwardServerChain extends ServerChain {
    public ForwardServerChain(Session session) {
        setSession(session);
    }

    public synchronized void response(ForwardResponse res) {
        sendAndReceive(res).expect(MessageTypes.HAPPY);
    }
}

