package net.result.sandnode.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.dto.LoginHistoryDTO;
import net.result.sandnode.dto.LoginResponseDTO;
import net.result.sandnode.exception.ProtocolException;
import net.result.sandnode.exception.error.ExpiredTokenException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.types.LoginHistoryResponse;
import net.result.sandnode.message.types.LoginRequest;
import net.result.sandnode.message.types.LoginResponse;
import net.result.sandnode.serverclient.SandnodeClient;

import java.util.List;

public class LoginClientChain extends ClientChain {
    public LoginClientChain(SandnodeClient client) {
        super(client);
    }

    public synchronized LoginResponseDTO login(String token)
            throws InterruptedException, SandnodeErrorException, ProtocolException {
        send(LoginRequest.byToken(token));
        var raw = receiveWithSpecifics(ExpiredTokenException.class);
        return new LoginResponse(raw).dto();
    }

    public synchronized List<LoginHistoryDTO> getHistory()
            throws ProtocolException, InterruptedException, SandnodeErrorException {
        var raw = sendAndReceive(LoginRequest.history());
        return new LoginHistoryResponse(raw).history();
    }
}
