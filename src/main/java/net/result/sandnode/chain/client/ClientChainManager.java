package net.result.sandnode.chain.client;

import net.result.sandnode.chain.ChainManager;
import net.result.sandnode.messages.RawMessage;

public interface ClientChainManager extends ChainManager {
    @Override
    ClientChain defaultChain(RawMessage message);
}
