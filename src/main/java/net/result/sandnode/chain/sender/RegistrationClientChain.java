package net.result.sandnode.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.RegistrationRequest;
import net.result.sandnode.message.types.RegistrationResponse;
import net.result.sandnode.util.IOController;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class RegistrationClientChain extends ClientChain {
    public RegistrationClientChain(IOController io) {
        super(io);
    }

    public synchronized String getTokenFromRegistration(
            @NotNull String nickname,
            @NotNull String password,
            @NotNull String device,
            @NotNull Map<String, String> keyStorage
    ) throws InterruptedException, ExpectedMessageException, SandnodeErrorException, UnknownSandnodeErrorException,
            UnprocessedMessagesException {
        RegistrationRequest request = new RegistrationRequest(nickname, password, device, keyStorage);
        send(request);

        RawMessage response = queue.take();
        ServerErrorManager.instance().handleError(response);

        return new RegistrationResponse(response).content();
    }
}
