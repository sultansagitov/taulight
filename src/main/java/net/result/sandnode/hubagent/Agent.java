package net.result.sandnode.hubagent;

import net.result.sandnode.message.util.NodeType;
import net.result.sandnode.encryption.GlobalKeyStorage;
import org.jetbrains.annotations.NotNull;

import static net.result.sandnode.message.util.NodeType.AGENT;

public abstract class Agent extends Node {
    public Agent(@NotNull GlobalKeyStorage globalKeyStorage) {
        super(globalKeyStorage);
    }

    @Override
    public @NotNull NodeType type() {
        return AGENT;
    }
}
