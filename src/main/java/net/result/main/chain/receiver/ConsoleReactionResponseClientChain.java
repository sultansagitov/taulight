package net.result.main.chain.receiver;

import net.result.sandnode.serverclient.SandnodeClient;
import net.result.taulight.chain.receiver.ReactionResponseClientChain;
import net.result.taulight.dto.ReactionDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConsoleReactionResponseClientChain extends ReactionResponseClientChain {
    private static final Logger LOGGER = LogManager.getLogger(ConsoleReactionResponseClientChain.class);

    public ConsoleReactionResponseClientChain(SandnodeClient client) {
        super(client);
    }

    @Override
    protected void onReaction(ReactionDTO reaction, boolean yourSession) {
        LOGGER.info("Reacted by {} {}", yourSession ? "you" : "someone", reaction);
    }
}
