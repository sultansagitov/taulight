package net.result.sandnode.chain.sender;

import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.message.types.NameRequest;
import net.result.sandnode.message.types.NameResponse;
import net.result.sandnode.util.IOController;

public class NameClientChain extends ClientChain {
    public NameClientChain(IOController io) {
        super(io);
    }

    public String getName() throws UnprocessedMessagesException, InterruptedException, ExpectedMessageException {
        send(new NameRequest());
        return new NameResponse(queue.take()).content();
    }
}
