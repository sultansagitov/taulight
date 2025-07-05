package net.result.taulight.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.UnknownSandnodeErrorException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.taulight.message.types.UseCodeRequest;

public class UseCodeClientChain extends ClientChain {
    public UseCodeClientChain(SandnodeClient client) {
        super(client);
    }

    public void use(String code) throws InterruptedException, UnprocessedMessagesException, ExpectedMessageException,
            UnknownSandnodeErrorException, SandnodeErrorException {
        UseCodeRequest request = new UseCodeRequest(code);
        send(request);

        RawMessage raw = receive();
        ServerErrorManager.instance().handleError(raw);

        new HappyMessage(raw);
    }
}
