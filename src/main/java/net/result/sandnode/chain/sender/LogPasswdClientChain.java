package net.result.sandnode.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.dto.LogPasswdResponseDTO;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.types.LogPasswdRequest;
import net.result.sandnode.message.types.LogPasswdResponse;
import net.result.sandnode.serverclient.SandnodeClient;

public class LogPasswdClientChain extends ClientChain {
    public LogPasswdClientChain(SandnodeClient client) {
        super(client);
    }

    public LogPasswdResponseDTO getToken(String nickname, String password, String device)
            throws InterruptedException, SandnodeErrorException, ProtocolException {
        var raw = sendAndReceive(new LogPasswdRequest(nickname, password, device));
        return new LogPasswdResponse(raw).dto();
    }
}
