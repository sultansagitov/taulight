package net.result.taulight.chain.sender;

import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.message.types.DownstreamResponse;

public class DownstreamServerChain extends ServerChain {
    public DownstreamServerChain(Session session) {
        setSession(session);
    }

    public synchronized void response(DownstreamResponse res) {
        sendAndReceive(res).expect(MessageTypes.HAPPY);
    }
}

