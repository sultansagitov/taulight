package net.result.taulight.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.UnknownSandnodeErrorException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.taulight.dto.CodeDTO;
import net.result.taulight.message.types.CheckCodeRequest;
import net.result.taulight.message.types.CheckCodeResponse;

public class CheckCodeClientChain extends ClientChain {
    public CheckCodeClientChain(SandnodeClient client) {
        super(client);
    }

    public CodeDTO check(String code) throws UnprocessedMessagesException, InterruptedException,
            ExpectedMessageException, UnknownSandnodeErrorException, SandnodeErrorException, DeserializationException {
        send(new CheckCodeRequest(code));
        RawMessage raw = receive();

        return new CheckCodeResponse(raw).getCode();
    }
}
