package net.result.main.chains;

import net.result.sandnode.chain.Chain;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.chain.client.BSTClientChainManager;
import net.result.sandnode.util.IOControl;

public class ConsoleClientChainManager extends BSTClientChainManager {
    public ConsoleClientChainManager(IOControl io) {
        super(io);
    }

    @Override
    public Chain defaultChain(RawMessage message) {
        return new ConsoleClientChain(this.io);
    }
}
