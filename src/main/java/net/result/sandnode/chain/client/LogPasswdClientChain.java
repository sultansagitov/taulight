package net.result.sandnode.chain.client;

import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.*;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.util.IOController;

public class LogPasswdClientChain extends ClientChain {
    public LogPasswdClientChain(IOController io) {
        super(io);
    }

    public String getToken(String nickname, String password) throws InterruptedException, SandnodeErrorException,
            ExpectedMessageException, UnknownSandnodeErrorException, UnprocessedMessagesException {
        LogPasswdRequest loginRequest = new LogPasswdRequest(nickname, password);
        send(loginRequest);

        RawMessage message = queue.take();

        if (message.headers().type() == MessageTypes.ERR) {
            ErrorMessage errorMessage = new ErrorMessage(message);
            ServerErrorManager.instance().throwAll(errorMessage.error);
        }

        LogPasswdResponse loginResponse = new LogPasswdResponse(message);

        return loginResponse.getToken();
    }
}
