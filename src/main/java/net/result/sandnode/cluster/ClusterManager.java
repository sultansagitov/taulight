package net.result.sandnode.cluster;

import net.result.sandnode.serverclient.Session;
import org.jetbrains.annotations.NotNull;

public interface ClusterManager {
    Cluster get(@NotNull String clusterID);

    void removeSession(Session session);
}
