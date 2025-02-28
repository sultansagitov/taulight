package net.result.sandnode.chain.client;

import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.ErrorMessage;
import net.result.sandnode.message.types.LoginRequest;
import net.result.sandnode.message.types.LoginResponse;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.util.IOController;

public class LoginClientChain extends ClientChain {
    private final String token;
    public String memberID;

    public LoginClientChain(IOController io, String token) {
        super(io);
        this.token = token;
    }

    @Override
    public void sync() throws InterruptedException, DeserializationException, ExpectedMessageException,
            SandnodeErrorException, UnknownSandnodeErrorException {
        LoginRequest loginRequest = new LoginRequest(token);
        send(loginRequest);

        RawMessage message = queue.take();

        if (message.headers().type() == MessageTypes.ERR) {
            ErrorMessage errorMessage = new ErrorMessage(message);
            ServerErrorManager.instance().throwAll(errorMessage.error);
        }

        LoginResponse loginResponse = new LoginResponse(message);

        memberID = loginResponse.getMemberID();
    }
}
