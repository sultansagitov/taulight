package net.result.sandnode.hubagent;

import net.result.sandnode.chain.ServerChainManager;
import net.result.sandnode.encryption.KeyStorageRegistry;
import net.result.sandnode.message.util.NodeType;
import org.jetbrains.annotations.NotNull;

public abstract class Node {
    public final KeyStorageRegistry keyStorageRegistry;

    public Node(@NotNull KeyStorageRegistry keyStorageRegistry) {
        this.keyStorageRegistry = keyStorageRegistry;
    }

    public Agent agent() {
        return (Agent) this;
    }

    public Hub hub() {
        return (Hub) this;
    }

    public abstract @NotNull ServerChainManager createChainManager();

    public abstract @NotNull NodeType type();

    @SuppressWarnings("EmptyMethod")
    public void close() {}

    @Override
    public String toString() {
        return "<%s %s>".formatted(getClass().getSimpleName(), keyStorageRegistry);
    }
}
