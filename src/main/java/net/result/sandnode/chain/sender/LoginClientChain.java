package net.result.sandnode.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.dto.LoginHistoryDTO;
import net.result.sandnode.dto.LoginResponseDTO;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.ExpiredTokenException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.LoginHistoryResponse;
import net.result.sandnode.message.types.LoginRequest;
import net.result.sandnode.message.types.LoginResponse;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.serverclient.SandnodeClient;

import java.util.List;

public class LoginClientChain extends ClientChain {
    public LoginClientChain(SandnodeClient client) {
        super(client);
    }

    public synchronized LoginResponseDTO login(String token)
            throws InterruptedException, DeserializationException, SandnodeErrorException, ProtocolException {
        LoginRequest loginRequest = LoginRequest.byToken(new Headers(), token);
        send(loginRequest);

        RawMessage raw = receiveWithSpecifics(ExpiredTokenException.class);

        LoginResponse loginResponse = new LoginResponse(raw);

        return loginResponse.dto();
    }

    public synchronized List<LoginHistoryDTO> getHistory() throws UnprocessedMessagesException, InterruptedException,
            UnknownSandnodeErrorException, SandnodeErrorException, DeserializationException, ExpectedMessageException {
        RawMessage raw = sendAndReceive(LoginRequest.history(new Headers()));
        return new LoginHistoryResponse(raw).history();
    }
}
