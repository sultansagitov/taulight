package net.result.sandnode.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.exception.ProtocolException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.types.WhoAmIRequest;
import net.result.sandnode.message.types.WhoAmIResponse;
import net.result.sandnode.serverclient.SandnodeClient;

public class WhoAmIClientChain extends ClientChain {
    public WhoAmIClientChain(SandnodeClient client) {
        super(client);
    }

    public synchronized String getNickname() throws InterruptedException, ProtocolException,
            SandnodeErrorException {
        var raw = sendAndReceive(new WhoAmIRequest());
        String nickname = new WhoAmIResponse(raw).getNickname();
        client.nickname = nickname;
        return nickname;
    }
}
