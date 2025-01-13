package net.result.main.chains;

import net.result.main.chains.client.ConsoleClientChain;
import net.result.sandnode.chain.client.ClientChain;
import net.result.sandnode.messages.RawMessage;
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
