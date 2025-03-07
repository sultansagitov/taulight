package net.result.sandnode.chain.client;

import net.result.sandnode.chain.ChainManager;
import net.result.sandnode.util.IOController;

public interface ClientChainManager extends ChainManager {

    void setIOController(IOController io);

}
