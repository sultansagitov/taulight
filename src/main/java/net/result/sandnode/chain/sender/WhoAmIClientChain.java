package net.result.sandnode.chain.sender;

import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.UnknownSandnodeErrorException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.WhoAmIRequest;
import net.result.sandnode.message.types.WhoAmIResponse;
import net.result.sandnode.util.IOController;

public class WhoAmIClientChain extends ClientChain {

    public WhoAmIClientChain(IOController io) {
        super(io);
    }

    public synchronized String getUserID() throws InterruptedException, ExpectedMessageException,
            UnknownSandnodeErrorException, SandnodeErrorException, UnprocessedMessagesException {
        send(new WhoAmIRequest());
        RawMessage raw = queue.take();

        ServerErrorManager.instance().handleError(raw);

        return new WhoAmIResponse(raw).getID();
    }

}
