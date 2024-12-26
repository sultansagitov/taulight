package net.result.sandnode.chain.server;

import net.result.sandnode.server.Session;
import net.result.sandnode.chain.IChainManager;

public interface IServerChainManager extends IChainManager {
    void setSession(Session session);

}
