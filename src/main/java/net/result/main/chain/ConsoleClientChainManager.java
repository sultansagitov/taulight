package net.result.main.chain;

import net.result.main.chain.client.ConsoleClientChain;
import net.result.sandnode.chain.client.ClientChain;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.chain.client.BSTClientChainManager;

public class ConsoleClientChainManager extends BSTClientChainManager {
    public ConsoleClientChainManager() {
        super();
    }

    @Override
    public ClientChain defaultChain(RawMessage message) {
        return new ConsoleClientChain(io);
    }
}
