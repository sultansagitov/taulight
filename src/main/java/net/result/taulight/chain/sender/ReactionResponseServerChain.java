package net.result.taulight.chain.sender;

import net.result.sandnode.chain.receiver.ServerChain;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.UnknownSandnodeErrorException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.db.ReactionEntryEntity;
import net.result.taulight.message.types.ReactionResponse;

public class ReactionResponseServerChain extends ServerChain {
    public ReactionResponseServerChain(Session session) {
        super(session);
    }

    public synchronized void reaction(ReactionEntryEntity reactionEntry, boolean yourSession)
            throws UnprocessedMessagesException, InterruptedException, ExpectedMessageException,
            UnknownSandnodeErrorException, SandnodeErrorException {
        send(new ReactionResponse(true, reactionEntry, yourSession));
        RawMessage raw = queue.take();
        ServerErrorManager.instance().handleError(raw);
        new HappyMessage(raw);
    }

    public synchronized void unreaction(String nickname, String packageName, String reaction, boolean yourSession)
            throws UnprocessedMessagesException, InterruptedException, ExpectedMessageException,
            UnknownSandnodeErrorException, SandnodeErrorException {
        send(new ReactionResponse(false, nickname, packageName, reaction, yourSession));
        RawMessage raw = queue.take();
        ServerErrorManager.instance().handleError(raw);
        new HappyMessage(raw);
    }
}