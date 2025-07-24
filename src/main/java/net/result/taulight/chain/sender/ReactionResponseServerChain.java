package net.result.taulight.chain.sender;

import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.exception.ProtocolException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.db.MessageEntity;
import net.result.taulight.db.ReactionEntryEntity;
import net.result.taulight.db.ReactionTypeEntity;
import net.result.taulight.message.types.ReactionResponse;

public class ReactionResponseServerChain extends ServerChain {
    public ReactionResponseServerChain(Session session) {
        super(session);
    }

    public synchronized void reaction(ReactionEntryEntity reactionEntry, boolean yourSession)
            throws ProtocolException, InterruptedException, SandnodeErrorException {
        sendAndReceive(new ReactionResponse(true, reactionEntry, yourSession)).expect(MessageTypes.HAPPY);
    }

    public synchronized void unreaction(
            String nickname,
            MessageEntity message,
            ReactionTypeEntity reactionType,
            boolean yourSession
    ) throws ProtocolException, InterruptedException,
            SandnodeErrorException {
        var response = new ReactionResponse(
                false,
                nickname,
                message.chat().id(),
                message.id(),
                reactionType.reactionPackage().name(),
                reactionType.name(),
                yourSession
        );
        sendAndReceive(response).expect(MessageTypes.HAPPY);
    }
}