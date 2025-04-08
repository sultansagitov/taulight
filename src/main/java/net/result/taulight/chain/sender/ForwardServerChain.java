package net.result.taulight.chain.sender;

import net.result.sandnode.chain.receiver.ServerChain;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.message.types.ForwardResponse;

public class ForwardServerChain extends ServerChain {
    public ForwardServerChain(Session session) {
        super(session);
    }

    public synchronized void response(ForwardResponse res)
            throws UnprocessedMessagesException, InterruptedException {
        send(res);
    }
}

