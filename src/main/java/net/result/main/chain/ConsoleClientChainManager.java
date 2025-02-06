package net.result.main.chain;

import net.result.main.chain.client.ConsoleForwardClientChain;
import net.result.main.chain.client.ConsoleForwardRequestClientChain;
import net.result.sandnode.chain.client.ClientChain;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.chain.client.BSTClientChainManager;
import net.result.sandnode.message.util.MessageType;
import net.result.taulight.message.TauMessageTypes;

public class ConsoleClientChainManager extends BSTClientChainManager {
    public ConsoleClientChainManager() {
        super();
    }

    @Override
    public ClientChain defaultChain(RawMessage message) {
        MessageType type = message.getHeaders().getType();
        if (type instanceof TauMessageTypes) {
            switch ((TauMessageTypes) type) {
                case FWD -> {
                    return new ConsoleForwardClientChain(io);
                }
            }
        }

        return new ConsoleForwardRequestClientChain(io);
    }
}
