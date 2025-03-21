package net.result.sandnode.chain.receiver;

import net.result.sandnode.serverclient.Session;
import net.result.sandnode.chain.ChainManager;

public interface ServerChainManager extends ChainManager {
    void setSession(Session session);
}
