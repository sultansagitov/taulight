package net.result.sandnode.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.UnknownSandnodeErrorException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.EmptyMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.util.IOController;

public class LogoutClientChain extends ClientChain {
    public LogoutClientChain(IOController io) {
        super(io);
    }

    public synchronized void logout() throws UnprocessedMessagesException, InterruptedException,
            ExpectedMessageException, UnknownSandnodeErrorException, SandnodeErrorException {
        send(new EmptyMessage(new Headers().setType(MessageTypes.LOGOUT)));
        RawMessage raw = queue.take();
        ServerErrorManager.instance().handleError(raw);
        new HappyMessage(raw);
    }
}
