package net.result.sandnode.chain;

import net.result.sandnode.chain.receiver.ExitClientChain;
import net.result.sandnode.message.util.MessageType;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.serverclient.SandnodeClient;

public abstract class BaseClientChainManager extends BaseChainManager implements ClientChainManager {
    protected final SandnodeClient client;

    protected BaseClientChainManager(SandnodeClient client) {
        super();
        this.client = client;
    }

    @Override
    public ReceiverChain createChain(MessageType type) {
        return type == MessageTypes.EXIT ? new ExitClientChain(client) : null;
    }
}
