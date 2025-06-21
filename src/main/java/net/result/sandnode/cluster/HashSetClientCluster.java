package net.result.sandnode.cluster;

import org.jetbrains.annotations.NotNull;

public class HashSetClientCluster extends HashSetCluster implements ClientCluster {
    public HashSetClientCluster(@NotNull String id) {
        super("#%s".formatted(id));
    }
}
