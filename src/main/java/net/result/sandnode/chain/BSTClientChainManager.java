package net.result.sandnode.chain;

import net.result.sandnode.util.IOController;

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
