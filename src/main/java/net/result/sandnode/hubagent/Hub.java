package net.result.sandnode.hubagent;

import net.result.sandnode.config.HubConfig;
import net.result.sandnode.encryption.KeyStorageRegistry;
import net.result.sandnode.message.util.NodeType;
import org.jetbrains.annotations.NotNull;

public abstract class Hub extends Node {
    public final HubConfig config;

    public Hub(KeyStorageRegistry hubKeyStorage, HubConfig config) {
        super(hubKeyStorage);
        this.config = config;
    }

    @Override
    public @NotNull NodeType type() {
        return NodeType.HUB;
    }

}
