package net.result.sandnode.chain.client;

import net.result.sandnode.error.Errors;
import net.result.sandnode.error.SandnodeError;
import net.result.sandnode.exception.*;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.*;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.util.IOController;

public class LogPasswdClientChain extends ClientChain {
    private final String memberID;
    private final String password;
    public String token;

    public LogPasswdClientChain(IOController io, String memberID, String password) {
        super(io);
        this.memberID = memberID;
        this.password = password;
    }

    @Override
    public void sync() throws InterruptedException, DeserializationException, MemberNotFoundException,
            ExpectedMessageException, UnauthorizedException {
        LogPasswdRequest loginRequest = new LogPasswdRequest(memberID, password);
        send(loginRequest);

        RawMessage message = queue.take();

        if (message.getHeaders().getType() == MessageTypes.ERR) {
            ErrorMessage errorMessage = new ErrorMessage(message);
            SandnodeError error = errorMessage.error;
            if (error instanceof Errors sys) {
                switch (sys) {
                    case SERVER_ERROR -> throw new UnknownSandnodeErrorException();
                    case MEMBER_NOT_FOUND -> throw new MemberNotFoundException();
                    case UNAUTHORIZED -> throw new UnauthorizedException();
                }
            }
        }

        LogPasswdResponse loginResponse = new LogPasswdResponse(message);

        token = loginResponse.getToken();
    }
}
