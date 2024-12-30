package net.result.sandnode.chain.server;

import net.result.sandnode.server.Session;
import net.result.sandnode.chain.ChainManager;

public interface IServerChainManager extends ChainManager {
    void setSession(Session session);

}
