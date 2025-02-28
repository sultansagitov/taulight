package net.result.sandnode.chain.client;

import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.SandnodeErrorException;
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
    public void sync() throws InterruptedException, SandnodeErrorException, ExpectedMessageException,
            UnknownSandnodeErrorException {
        LogPasswdRequest loginRequest = new LogPasswdRequest(memberID, password);
        send(loginRequest);

        RawMessage message = queue.take();

        if (message.headers().type() == MessageTypes.ERR) {
            ErrorMessage errorMessage = new ErrorMessage(message);
            ServerErrorManager.instance().throwAll(errorMessage.error);
        }

        LogPasswdResponse loginResponse = new LogPasswdResponse(message);

        token = loginResponse.getToken();
    }
}
