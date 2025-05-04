package net.result.sandnode.chain;

import net.result.sandnode.serverclient.Session;

public interface ServerChainManager extends ChainManager {
    void setSession(Session session);
}
