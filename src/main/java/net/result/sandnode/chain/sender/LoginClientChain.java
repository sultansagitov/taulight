package net.result.sandnode.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.dto.LoginHistoryDTO;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.LoginHistoryResponse;
import net.result.sandnode.message.types.LoginRequest;
import net.result.sandnode.message.types.LoginResponse;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.util.IOController;

import java.util.List;

public class LoginClientChain extends ClientChain {
    public LoginClientChain(IOController io) {
        super(io);
    }

    public synchronized String getNickname(String token) throws InterruptedException, DeserializationException,
            SandnodeErrorException, UnknownSandnodeErrorException, UnprocessedMessagesException {
        LoginRequest loginRequest = LoginRequest.byToken(new Headers(), token);
        send(loginRequest);

        RawMessage raw = queue.take();

        ServerErrorManager.instance().handleError(raw);

        LoginResponse loginResponse = new LoginResponse(raw);

        return loginResponse.getNickname();
    }

    public synchronized List<LoginHistoryDTO> getHistory() throws UnprocessedMessagesException, InterruptedException,
            UnknownSandnodeErrorException, SandnodeErrorException, DeserializationException, ExpectedMessageException {
        LoginRequest loginRequest = LoginRequest.history(new Headers());
        send(loginRequest);

        RawMessage raw = queue.take();

        ServerErrorManager.instance().handleError(raw);

        LoginHistoryResponse loginResponse = new LoginHistoryResponse(raw);

        return loginResponse.history();
    }
}
