package net.result.sandnode.chain.client;

import net.result.sandnode.chain.ChainManager;
import net.result.sandnode.message.util.MessageType;
import net.result.sandnode.util.IOController;
import org.jetbrains.annotations.Nullable;

public interface ClientChainManager extends ChainManager {

    void setIOController(IOController io);

    @Override
    default @Nullable ClientChain createChain(MessageType type) {
        return null;
    }
}
