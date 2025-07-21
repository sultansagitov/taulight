package net.result.sandnode.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.UnknownSandnodeErrorException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.WhoAmIRequest;
import net.result.sandnode.message.types.WhoAmIResponse;
import net.result.sandnode.serverclient.SandnodeClient;

public class WhoAmIClientChain extends ClientChain {
    public WhoAmIClientChain(SandnodeClient client) {
        super(client);
    }

    public synchronized String getNickname() throws InterruptedException, ExpectedMessageException,
            UnknownSandnodeErrorException, SandnodeErrorException, UnprocessedMessagesException {
        RawMessage raw = sendAndReceive(new WhoAmIRequest());
        return new WhoAmIResponse(raw).getNickname();
    }
}
