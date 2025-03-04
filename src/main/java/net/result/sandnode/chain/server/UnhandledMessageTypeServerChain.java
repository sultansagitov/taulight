package net.result.sandnode.chain.server;

import net.result.sandnode.error.Errors;
import net.result.sandnode.serverclient.Session;

public class UnhandledMessageTypeServerChain extends ServerChain {
    public UnhandledMessageTypeServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws Exception {
        queue.take();
        sendFin(Errors.UNHANDLED_MESSAGE_TYPE.createMessage());
    }
}
