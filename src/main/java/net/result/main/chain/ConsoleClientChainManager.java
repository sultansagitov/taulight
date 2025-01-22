package net.result.main.chain;

import net.result.main.chain.client.ConsoleClientChain;
import net.result.sandnode.chain.client.ClientChain;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.chain.client.BSTClientChainManager;
import net.result.sandnode.util.IOControl;

public class ConsoleClientChainManager extends BSTClientChainManager {
    public ConsoleClientChainManager(IOControl io) {
        super(io);
    }

    @Override
    public ClientChain defaultChain(RawMessage message) {
        return new ConsoleClientChain(io);
    }
}
