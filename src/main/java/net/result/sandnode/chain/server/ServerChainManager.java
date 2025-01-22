package net.result.sandnode.chain.server;

import net.result.sandnode.message.RawMessage;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.chain.ChainManager;

public interface ServerChainManager extends ChainManager {
    void setSession(Session session);

    @Override
    ServerChain defaultChain(RawMessage message);
}
