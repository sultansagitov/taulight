package net.result.taulight.hubagent;

import net.result.sandnode.chain.server.ServerChainManager;
import net.result.sandnode.hubagent.Hub;
import net.result.sandnode.encryption.KeyStorageRegistry;
import net.result.taulight.chain.TauBSTServerChainManager;
import org.jetbrains.annotations.NotNull;

public class TauHub extends Hub {
    public TauHub(KeyStorageRegistry hubKeyStorage) {
        super(hubKeyStorage);
    }

    @Override
    public @NotNull ServerChainManager createChainManager() {
        return new TauBSTServerChainManager();
    }
}