package net.result.sandnode.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.message.types.WhoAmIRequest;
import net.result.sandnode.message.types.WhoAmIResponse;
import net.result.sandnode.serverclient.Session;

public class WhoAmIServerChain extends ServerChain implements ReceiverChain {
    public WhoAmIServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws Exception {
        new WhoAmIRequest(queue.take());

        if (session.member == null) throw new UnauthorizedException();

        sendFin(new WhoAmIResponse(session.member));
    }
}
