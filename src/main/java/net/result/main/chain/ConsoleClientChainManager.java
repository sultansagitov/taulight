package net.result.main.chain;

import net.result.main.chain.client.ConsoleForwardClientChain;
import net.result.sandnode.chain.client.UnhandledMessageTypeClientChain;
import net.result.sandnode.chain.client.ClientChain;
import net.result.sandnode.chain.client.BSTClientChainManager;
import net.result.sandnode.message.util.MessageType;
import net.result.taulight.message.TauMessageTypes;
import org.jetbrains.annotations.Nullable;

public class ConsoleClientChainManager extends BSTClientChainManager {
    public ConsoleClientChainManager() {
        super();
    }

    @Override
    public @Nullable ClientChain createChain(MessageType type) {
        if (type == TauMessageTypes.FWD) {
            return new ConsoleForwardClientChain(io);
        }

        return new UnhandledMessageTypeClientChain(io);
    }
}
