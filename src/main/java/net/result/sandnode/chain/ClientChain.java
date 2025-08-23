package net.result.sandnode.chain;

import net.result.sandnode.serverclient.SandnodeClient;

public abstract class ClientChain extends BaseChain {
    protected final SandnodeClient client;

    public ClientChain(SandnodeClient client) {
        setIO(client.io());
        this.client = client;
    }
}
