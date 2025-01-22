package net.result.sandnode.chain.client;

import net.result.sandnode.exception.*;
import net.result.sandnode.message.IMessage;
import net.result.sandnode.message.types.ErrorMessage;
import net.result.sandnode.message.types.RegistrationRequest;
import net.result.sandnode.message.types.RegistrationResponse;
import net.result.sandnode.error.Errors;
import net.result.sandnode.error.SandnodeError;
import net.result.sandnode.util.IOControl;

import static net.result.sandnode.message.util.MessageTypes.ERR;

public class RegistrationClientChain extends ClientChain {
    public String token;
    private final String memberID;
    private final String password;

    public RegistrationClientChain(IOControl io, String memberID, String password) {
        super(io);
        this.memberID = memberID;
        this.password = password;
    }

    @Override
    public void sync() throws InterruptedException, ExpectedMessageException, BusyMemberIDException,
            DeserializationException, InvalidMemberIDPassword {
        RegistrationRequest request = new RegistrationRequest(memberID, password);
        send(request);

        IMessage response = queue.take();

        if (response.getHeaders().getType() == ERR) {
            ErrorMessage errorMessage = new ErrorMessage(response);
            SandnodeError type = errorMessage.error;
            if (type instanceof Errors enumError) {
                switch (enumError) {
                    case INVALID_MEMBER_ID_OR_PASSWORD -> throw new InvalidMemberIDPassword();
                    case MEMBER_ID_BUSY -> throw new BusyMemberIDException(memberID);
                }
            }
        }

        token = new RegistrationResponse(response).getToken();
    }
}
