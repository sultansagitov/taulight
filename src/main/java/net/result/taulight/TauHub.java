package net.result.taulight;

import net.result.sandnode.chain.server.ServerChainManager;
import net.result.sandnode.Hub;
import net.result.sandnode.encryption.GlobalKeyStorage;
import net.result.taulight.chain.TauBSTServerChainManager;
import org.jetbrains.annotations.NotNull;

public class TauHub extends Hub {

    public final TauChatManager chatManager;

    public TauHub(GlobalKeyStorage hubKeyStorage, TauChatManager chatManager) {
        super(hubKeyStorage);
        this.chatManager = chatManager;
    }

    @Override
    public @NotNull ServerChainManager createChainManager() {
        return new TauBSTServerChainManager();
    }

    @Override
    public void close() {
    }
}