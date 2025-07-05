package net.result.sandnode.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.dto.LogPasswdResponseDTO;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.*;
import net.result.sandnode.serverclient.SandnodeClient;

public class LogPasswdClientChain extends ClientChain {
    public LogPasswdClientChain(SandnodeClient client) {
        super(client);
    }

    public LogPasswdResponseDTO getToken(String nickname, String password, String device)
            throws InterruptedException, SandnodeErrorException, ExpectedMessageException,
            UnknownSandnodeErrorException, UnprocessedMessagesException, DeserializationException {
        LogPasswdRequest loginRequest = new LogPasswdRequest(nickname, password, device);
        send(loginRequest);

        RawMessage message = receive();
        ServerErrorManager.instance().handleError(message);

        LogPasswdResponse loginResponse = new LogPasswdResponse(message);

        return loginResponse.dto();
    }
}
