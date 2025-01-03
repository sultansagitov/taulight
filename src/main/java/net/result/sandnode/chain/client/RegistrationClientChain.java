package net.result.sandnode.chain.client;

import net.result.sandnode.exceptions.*;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.types.RegistrationRequest;
import net.result.sandnode.messages.types.RegistrationResponse;
import net.result.sandnode.util.IOControl;

import static net.result.sandnode.messages.util.MessageTypes.ERR;

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
    public void sync() throws InterruptedException, ExpectedMessageException, BusyMemberIDException {
        RegistrationRequest request = new RegistrationRequest(memberID, password);
        send(request);

        IMessage response = queue.take();

        if (response.getHeaders().getType() == ERR) {
            throw new BusyMemberIDException(memberID);
        }

        token = new RegistrationResponse(response).getToken();
    }
}
