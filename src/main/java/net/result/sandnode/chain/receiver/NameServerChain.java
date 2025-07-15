package net.result.sandnode.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.hubagent.Hub;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.NameRequest;
import net.result.sandnode.message.types.NameResponse;
import net.result.sandnode.serverclient.Session;

public class NameServerChain extends ServerChain implements ReceiverChain {
    public NameServerChain(Session session) {
        super(session);
    }

    @Override
    public NameResponse handle(RawMessage raw) throws Exception {
        new NameRequest(raw);
        Hub hub = session.server.node.hub();
        return new NameResponse(hub.config.name());
    }
}
