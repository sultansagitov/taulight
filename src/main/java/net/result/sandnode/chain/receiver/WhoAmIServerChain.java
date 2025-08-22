package net.result.sandnode.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.WhoAmIRequest;
import net.result.sandnode.message.types.WhoAmIResponse;

public class WhoAmIServerChain extends ServerChain implements ReceiverChain {
    @Override
    public WhoAmIResponse handle(RawMessage raw) {
        new WhoAmIRequest(raw);

        if (session.member == null) throw new UnauthorizedException();

        return new WhoAmIResponse(session.member.nickname());
    }
}
