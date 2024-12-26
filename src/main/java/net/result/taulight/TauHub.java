package net.result.taulight;

import net.result.sandnode.chain.server.IServerChainManager;
import net.result.sandnode.Hub;
import net.result.sandnode.encryption.GlobalKeyStorage;
import org.jetbrains.annotations.NotNull;

public class TauHub extends Hub {

    public TauHub(GlobalKeyStorage hubKeyStorage) {
        super(hubKeyStorage);
    }

    @Override
    public @NotNull IServerChainManager createChainManager() {
        return new TauBSTServerChainManager();
    }

    @Override
    public void close() {
    }
}