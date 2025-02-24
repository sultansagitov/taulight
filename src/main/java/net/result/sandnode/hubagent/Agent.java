package net.result.sandnode.hubagent;

import net.result.sandnode.message.util.NodeType;
import net.result.sandnode.encryption.KeyStorageRegistry;
import org.jetbrains.annotations.NotNull;

public abstract class Agent extends Node {
    public Agent(@NotNull KeyStorageRegistry keyStorageRegistry) {
        super(keyStorageRegistry);
    }

    @Override
    public @NotNull NodeType type() {
        return NodeType.AGENT;
    }
}
