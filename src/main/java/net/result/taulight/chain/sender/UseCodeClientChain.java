package net.result.taulight.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.exception.ProtocolException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.taulight.message.types.UseCodeRequest;

public class UseCodeClientChain extends ClientChain {
    public UseCodeClientChain(SandnodeClient client) {
        super(client);
    }

    public void use(String code) throws InterruptedException, ProtocolException, SandnodeErrorException {
        var raw = sendAndReceive(new UseCodeRequest(code));
        new HappyMessage(raw);
    }
}
