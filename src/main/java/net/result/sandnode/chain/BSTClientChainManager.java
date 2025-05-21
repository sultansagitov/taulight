package net.result.sandnode.chain;

import net.result.sandnode.serverclient.SandnodeClient;
import net.result.sandnode.util.IOController;

public abstract class BSTClientChainManager extends BSTChainManager implements ClientChainManager {
    protected SandnodeClient client;
    protected IOController io;

    public BSTClientChainManager(SandnodeClient client) {
        super();
        this.client = client;
    }

    @Override
    public void setIOController(IOController io) {
        this.io = io;
    }
}
