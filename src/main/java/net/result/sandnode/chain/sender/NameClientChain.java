package net.result.sandnode.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.exception.ProtocolException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.types.NameRequest;
import net.result.sandnode.message.types.NameResponse;
import net.result.sandnode.serverclient.SandnodeClient;

public class NameClientChain extends ClientChain {
    public NameClientChain(SandnodeClient client) {
        super(client);
    }

    public String getName() throws ProtocolException, InterruptedException, SandnodeErrorException {
        var raw = sendAndReceive(new NameRequest());
        return new NameResponse(raw).content();
    }
}
