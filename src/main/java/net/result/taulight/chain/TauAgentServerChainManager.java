package net.result.taulight.chain;

import net.result.sandnode.chain.HubServerChainManager;
import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChainManager;
import net.result.sandnode.message.util.MessageType;

public class TauAgentServerChainManager extends HubServerChainManager implements ServerChainManager {
    public TauAgentServerChainManager() {
        super();
    }

    @Override
    public ReceiverChain createChain(MessageType type) {
        return super.createChain(type);
    }
}
