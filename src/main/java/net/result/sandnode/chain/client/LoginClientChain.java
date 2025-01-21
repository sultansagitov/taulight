package net.result.sandnode.chain.client;

import net.result.sandnode.exceptions.*;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.types.ErrorMessage;
import net.result.sandnode.messages.types.LoginRequest;
import net.result.sandnode.messages.types.LoginResponse;
import net.result.sandnode.server.Errors;
import net.result.sandnode.server.SandnodeError;
import net.result.sandnode.util.IOControl;

import static net.result.sandnode.messages.util.MessageTypes.ERR;

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
