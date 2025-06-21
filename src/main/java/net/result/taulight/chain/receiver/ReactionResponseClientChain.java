package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.taulight.dto.ReactionDTO;
import net.result.taulight.message.types.ReactionResponse;

public abstract class ReactionResponseClientChain extends ClientChain implements ReceiverChain {
    public ReactionResponseClientChain(SandnodeClient client) {
        super(client);
    }

    @Override
    public void sync() throws Exception {
        ReactionResponse response = new ReactionResponse(queue.take());
        ReactionDTO reaction = response.getReaction();
        onReaction(reaction, response.isYourSession());
        send(new HappyMessage());
    }

    protected abstract void onReaction(ReactionDTO reaction, boolean yourSession);
}