package net.result.sandnode.chain.receiver;

import net.result.sandnode.util.Logout;
import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.serverclient.Session;

public class LogoutServerChain extends ServerChain implements ReceiverChain {
    public LogoutServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws Exception {
        queue.take();

        if (session.member == null) throw new UnauthorizedException();

        Logout.logout(session);

        send(new HappyMessage());
    }
}
