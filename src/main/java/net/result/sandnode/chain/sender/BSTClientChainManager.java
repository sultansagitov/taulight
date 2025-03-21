package net.result.sandnode.chain.sender;

import net.result.sandnode.util.IOController;
import net.result.sandnode.chain.BSTChainManager;

public abstract class BSTClientChainManager extends BSTChainManager implements ClientChainManager {
    protected IOController io;

    public BSTClientChainManager() {
        super();
    }

    @Override
    public void setIOController(IOController io) {
        this.io = io;
    }
}
