package net.result.sandnode.chain;

import net.result.sandnode.util.IOController;

public interface ClientChainManager extends ChainManager {

    void setIOController(IOController io);

}
