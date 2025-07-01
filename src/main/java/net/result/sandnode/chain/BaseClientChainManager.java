package net.result.sandnode.chain;

import net.result.sandnode.serverclient.SandnodeClient;

public abstract class BaseClientChainManager extends BaseChainManager implements ClientChainManager {
    protected final SandnodeClient client;

    public BaseClientChainManager(SandnodeClient client) {
        super();
        this.client = client;
    }
}
