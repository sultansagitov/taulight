package net.result.taulight.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.exception.ProtocolException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.taulight.dto.CodeDTO;
import net.result.taulight.message.types.CheckCodeRequest;
import net.result.taulight.message.types.CheckCodeResponse;

public class CheckCodeClientChain extends ClientChain {
    public CheckCodeClientChain(SandnodeClient client) {
        super(client);
    }

    public CodeDTO check(String code) throws ProtocolException, InterruptedException, SandnodeErrorException {
        var raw = sendAndReceive(new CheckCodeRequest(code));
        return new CheckCodeResponse(raw).getCode();
    }
}
