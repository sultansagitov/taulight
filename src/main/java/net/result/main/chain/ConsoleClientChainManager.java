package net.result.main.chain;

import net.result.main.chain.receiver.ConsoleForwardClientChain;
import net.result.main.chain.receiver.ConsoleReactionResponseClientChain;
import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.receiver.UnhandledMessageTypeClientChain;
import net.result.sandnode.chain.BSTClientChainManager;
import net.result.sandnode.message.util.MessageType;
import net.result.taulight.message.TauMessageTypes;
import org.jetbrains.annotations.Nullable;

public class ConsoleClientChainManager extends BSTClientChainManager {
    public ConsoleClientChainManager() {
        super();
    }

    @Override
    public @Nullable ReceiverChain createChain(MessageType type) {
        return type instanceof TauMessageTypes tau ? switch (tau) {
            case FWD -> new ConsoleForwardClientChain(io);
            case REACTION -> new ConsoleReactionResponseClientChain(io);
            default -> new UnhandledMessageTypeClientChain(io);
        } : new UnhandledMessageTypeClientChain(io);
    }
}
