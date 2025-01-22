package net.result.sandnode.chain.client;

import net.result.sandnode.exception.*;
import net.result.sandnode.message.IMessage;
import net.result.sandnode.message.types.ErrorMessage;
import net.result.sandnode.message.types.LoginRequest;
import net.result.sandnode.message.types.LoginResponse;
import net.result.sandnode.error.Errors;
import net.result.sandnode.error.SandnodeError;
import net.result.sandnode.util.IOControl;

import static net.result.sandnode.message.util.MessageTypes.ERR;

public class LoginClientChain extends ClientChain {
    private final String token;
    public String memberID;

    public LoginClientChain(IOControl io, String token) {
        super(io);
        this.token = token;
    }

    @Override
    public void sync()
            throws InterruptedException, MemberNotFoundException, DeserializationException, InvalidTokenException {
        LoginRequest loginRequest = new LoginRequest(token);
        send(loginRequest);

        IMessage message = queue.take();

        if (message.getHeaders().getType() == ERR) {
            ErrorMessage errorMessage = new ErrorMessage(message);
            SandnodeError error = errorMessage.error;
            if (error instanceof Errors enumError) {
                switch (enumError) {
                    case INVALID_TOKEN -> throw new InvalidTokenException();
                    case MEMBER_NOT_FOUND -> throw new MemberNotFoundException();
                }
            }
        }

        LoginResponse loginResponse = new LoginResponse(message);

        memberID = loginResponse.getMemberID();
    }
}
