package net.result.sandnode.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.hubagent.Hub;
import net.result.sandnode.message.types.NameRequest;
import net.result.sandnode.message.types.NameResponse;
import net.result.sandnode.serverclient.Session;

public class NameServerChain extends ServerChain implements ReceiverChain {
    public NameServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws UnprocessedMessagesException, InterruptedException, ExpectedMessageException {
        new NameRequest(queue.take());
        Hub hub = (Hub) session.server.node;
        sendFin(new NameResponse(hub.config.name()));
    }
}
