package net.result.sandnode.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.dto.LogPasswdResponseDTO;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.UnknownSandnodeErrorException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.types.LogPasswdRequest;
import net.result.sandnode.message.types.LogPasswdResponse;
import net.result.sandnode.serverclient.SandnodeClient;

public class LogPasswdClientChain extends ClientChain {
    public LogPasswdClientChain(SandnodeClient client) {
        super(client);
    }

    public LogPasswdResponseDTO getToken(String nickname, String password, String device)
            throws InterruptedException, SandnodeErrorException, ExpectedMessageException,
            UnknownSandnodeErrorException, UnprocessedMessagesException, DeserializationException {
        var raw = sendAndReceive(new LogPasswdRequest(nickname, password, device));
        return new LogPasswdResponse(raw).dto();
    }
}
