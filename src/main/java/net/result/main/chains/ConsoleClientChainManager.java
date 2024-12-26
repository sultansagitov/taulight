package net.result.main.chains;

import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.chain.client.BSTClientChainManager;
import net.result.sandnode.chain.IChain;
import net.result.sandnode.util.IOControl;

public class ConsoleClientChainManager extends BSTClientChainManager {
    public ConsoleClientChainManager(IOControl io) {
        super(io);
    }

    @Override
    public IChain defaultChain(RawMessage message) {
        return new ConsoleClientChain(this.io);
    }
}
