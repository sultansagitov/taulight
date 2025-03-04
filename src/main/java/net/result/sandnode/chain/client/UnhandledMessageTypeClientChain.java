package net.result.sandnode.chain.client;

import net.result.sandnode.error.Errors;
import net.result.sandnode.util.IOController;

public class UnhandledMessageTypeClientChain extends ClientChain {
    public UnhandledMessageTypeClientChain(IOController io) {
        super(io);
    }

    @Override
    public void sync() throws Exception {
        queue.take();
        sendFin(Errors.UNHANDLED_MESSAGE_TYPE.createMessage());
    }
}
