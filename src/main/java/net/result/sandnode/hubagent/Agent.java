package net.result.sandnode.hubagent;

import net.result.sandnode.config.AgentConfig;
import net.result.sandnode.message.util.NodeType;
import net.result.sandnode.encryption.KeyStorageRegistry;
import org.jetbrains.annotations.NotNull;

public abstract class Agent extends Node {
    public final AgentConfig config;

    public Agent(@NotNull KeyStorageRegistry keyStorageRegistry, AgentConfig config) {
        super(keyStorageRegistry);
        this.config = config;
    }

    @Override
    public @NotNull NodeType type() {
        return NodeType.AGENT;
    }
}
