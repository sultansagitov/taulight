package net.result.taulight.chain.sender;

import net.result.sandnode.chain.sender.ClientChain;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.exception.UnknownSandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.util.IOController;
import net.result.sandnode.message.types.HappyMessage;
import net.result.taulight.message.types.ReactionRequest;

import java.util.UUID;

public class ReactionClientChain extends ClientChain {

    public ReactionClientChain(IOController io) {
        super(io);
    }

    public synchronized void react(UUID messageID, String reaction)
            throws InterruptedException, ExpectedMessageException, SandnodeErrorException,
            UnknownSandnodeErrorException, UnprocessedMessagesException {
        send(ReactionRequest.react(messageID, reaction));
        RawMessage raw = queue.take();
        ServerErrorManager.instance().handleError(raw);
        new HappyMessage(raw);
    }

    public synchronized void unreact(UUID messageID, String reaction)
            throws InterruptedException, ExpectedMessageException, SandnodeErrorException,
            UnknownSandnodeErrorException, UnprocessedMessagesException {
        send(ReactionRequest.unreact(messageID, reaction));
        RawMessage raw = queue.take();
        ServerErrorManager.instance().handleError(raw);
        new HappyMessage(raw);
    }
}