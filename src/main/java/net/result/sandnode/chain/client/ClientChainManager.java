package net.result.sandnode.chain.client;

import net.result.sandnode.chain.ChainManager;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.util.IOController;

public interface ClientChainManager extends ChainManager {
    @Override
    ClientChain defaultChain(RawMessage message);

    void setIOController(IOController io);
}
