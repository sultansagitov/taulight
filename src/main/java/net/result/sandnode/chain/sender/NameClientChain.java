package net.result.sandnode.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.UnknownSandnodeErrorException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.NameRequest;
import net.result.sandnode.message.types.NameResponse;
import net.result.sandnode.serverclient.SandnodeClient;

public class NameClientChain extends ClientChain {
    public NameClientChain(SandnodeClient client) {
        super(client);
    }

    public String getName() throws UnprocessedMessagesException, InterruptedException, ExpectedMessageException,
            UnknownSandnodeErrorException, SandnodeErrorException {
        RawMessage raw = sendAndReceive(new NameRequest());
        return new NameResponse(raw).content();
    }
}
