package net.result.taulight.cluster;

import net.result.sandnode.cluster.ClusterManager;
import net.result.taulight.db.ChatEntity;

public interface TauClusterManager extends ClusterManager {
    ChatCluster getCluster(ChatEntity chat);
}
