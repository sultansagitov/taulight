package net.result.sandnode.chain.server;

import net.result.sandnode.db.Member;
import net.result.sandnode.error.Errors;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.types.WhoAmIRequest;
import net.result.sandnode.message.types.WhoAmIResponse;
import net.result.sandnode.serverclient.Session;

public class WhoAmIServerChain extends ServerChain {
    public WhoAmIServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException, ExpectedMessageException {
        new WhoAmIRequest(queue.take());

        Member member = session.member;

        if (member == null) {
            sendFin(Errors.UNAUTHORIZED.createMessage());
            return;
        }

        sendFin(new WhoAmIResponse(session.member));
    }
}
