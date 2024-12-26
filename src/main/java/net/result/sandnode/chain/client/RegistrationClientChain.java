package net.result.sandnode.chain.client;

import net.result.sandnode.exceptions.*;
import net.result.sandnode.messages.types.RegistrationRequest;
import net.result.sandnode.messages.types.RegistrationResponse;
import net.result.sandnode.util.IOControl;

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
    public void start() throws InterruptedException, ExpectedMessageException {
        RegistrationRequest request = new RegistrationRequest(memberID, password);
        send(request);

        token = new RegistrationResponse(queue.take()).getToken();
    }
}
