package net.result.sandnode.chain.client;

import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.ErrorMessage;
import net.result.sandnode.message.types.RegistrationRequest;
import net.result.sandnode.message.types.RegistrationResponse;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.util.IOController;

public class RegistrationClientChain extends ClientChain {
    public RegistrationClientChain(IOController io) {
        super(io);
    }

    public synchronized String getTokenFromRegistration(String nickname, String password)
            throws InterruptedException, ExpectedMessageException, SandnodeErrorException,
            UnknownSandnodeErrorException, UnprocessedMessagesException {
        RegistrationRequest request = new RegistrationRequest(nickname, password);
        send(request);

        RawMessage response = queue.take();

        if (response.headers().type() == MessageTypes.ERR) {
            ErrorMessage errorMessage = new ErrorMessage(response);
            ServerErrorManager.instance().throwAll(errorMessage.error);
        }

        return new RegistrationResponse(response).getToken();
    }
}
