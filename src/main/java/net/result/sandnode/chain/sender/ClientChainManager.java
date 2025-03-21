package net.result.sandnode.chain.sender;

import net.result.sandnode.chain.ChainManager;
import net.result.sandnode.util.IOController;

public interface ClientChainManager extends ChainManager {

    void setIOController(IOController io);

}
