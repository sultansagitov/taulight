package net.result.sandnode.chain.client;

import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.*;
import net.result.sandnode.message.IMessage;
import net.result.sandnode.message.types.ErrorMessage;
import net.result.sandnode.message.types.RegistrationRequest;
import net.result.sandnode.message.types.RegistrationResponse;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.util.IOController;

public class RegistrationClientChain extends ClientChain {
    public String token;
    private final String memberID;
    private final String password;

    public RegistrationClientChain(IOController io, String memberID, String password) {
        super(io);
        this.memberID = memberID;
        this.password = password;
    }

    @Override
    public void sync() throws InterruptedException, ExpectedMessageException, SandnodeErrorException,
            DeserializationException, UnknownSandnodeErrorException {
        RegistrationRequest request = new RegistrationRequest(memberID, password);
        send(request);

        IMessage response = queue.take();

        if (response.headers().type() == MessageTypes.ERR) {
            ErrorMessage errorMessage = new ErrorMessage(response);
            ServerErrorManager.instance().throwAll(errorMessage.error);

        }

        token = new RegistrationResponse(response).getToken();
    }
}
