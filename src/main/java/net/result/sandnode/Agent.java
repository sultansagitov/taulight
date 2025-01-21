package net.result.sandnode;

import net.result.sandnode.messages.util.NodeType;
import net.result.sandnode.encryption.GlobalKeyStorage;
import org.jetbrains.annotations.NotNull;

import static net.result.sandnode.messages.util.NodeType.AGENT;

public abstract class Agent extends Node {
    public Agent(@NotNull GlobalKeyStorage globalKeyStorage) {
        super(globalKeyStorage);
    }

    @Override
    public @NotNull NodeType type() {
        return AGENT;
    }
}
