package net.result.sandnode.chain;

import net.result.sandnode.serverclient.SandnodeClient;

public abstract class BSTClientChainManager extends BSTChainManager implements ClientChainManager {
    protected final SandnodeClient client;

    public BSTClientChainManager(SandnodeClient client) {
        super();
        this.client = client;
    }
}
