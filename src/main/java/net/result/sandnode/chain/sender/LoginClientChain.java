package net.result.sandnode.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.LoginRequest;
import net.result.sandnode.message.types.LoginResponse;
import net.result.sandnode.util.IOController;

public class LoginClientChain extends ClientChain {
    public LoginClientChain(IOController io) {
        super(io);
    }

    public String getNickname(String token) throws InterruptedException, DeserializationException,
            SandnodeErrorException, UnknownSandnodeErrorException, UnprocessedMessagesException {
        LoginRequest loginRequest = new LoginRequest(token);
        send(loginRequest);

        RawMessage message = queue.take();

        ServerErrorManager.instance().handleError(message);

        LoginResponse loginResponse = new LoginResponse(message);

        return loginResponse.getNickname();
    }
}
