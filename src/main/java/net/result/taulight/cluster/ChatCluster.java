package net.result.taulight.cluster;

import net.result.sandnode.cluster.HashSetCluster;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ChatCluster extends HashSetCluster {
    public ChatCluster(@NotNull UUID chatID) {
        super("@%s".formatted(chatID));
    }
}
