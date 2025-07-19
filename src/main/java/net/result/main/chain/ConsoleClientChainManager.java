package net.result.main.chain;

import net.result.main.chain.receiver.ConsoleForwardClientChain;
import net.result.main.chain.receiver.ConsoleReactionResponseClientChain;
import net.result.sandnode.chain.BaseClientChainManager;
import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.message.util.MessageType;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.taulight.message.TauMessageTypes;
import org.jetbrains.annotations.Nullable;

public class ConsoleClientChainManager extends BaseClientChainManager {
    public ConsoleClientChainManager(SandnodeClient client) {
        super(client);
    }

    @Override
    public @Nullable ReceiverChain createChain(MessageType type) {
        if (type instanceof TauMessageTypes tau) {
            ReceiverChain chain = switch (tau) {
                case FWD -> new ConsoleForwardClientChain(client);
                case REACTION -> new ConsoleReactionResponseClientChain(client);
                default -> null;
            };
            if (chain != null) return chain;
        }

        return super.createChain(type);
    }
}
