package net.result.taulight.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.taulight.message.types.ReactionRequest;

import java.util.UUID;

public class ReactionRequestClientChain extends ClientChain {

    public ReactionRequestClientChain(SandnodeClient client) {
        super(client);
    }

    public synchronized void react(UUID messageID, String reaction) {
        sendAndReceive(ReactionRequest.react(messageID, reaction)).expect(MessageTypes.HAPPY);
    }

    public synchronized void unreact(UUID messageID, String reaction) {
        sendAndReceive(ReactionRequest.unreact(messageID, reaction)).expect(MessageTypes.HAPPY);
    }
}