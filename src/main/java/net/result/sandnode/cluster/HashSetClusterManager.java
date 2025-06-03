package net.result.sandnode.cluster;

import net.result.sandnode.serverclient.Session;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

public class HashSetClusterManager implements ClusterManager {
    private final Collection<Cluster> clusters;

    public HashSetClusterManager() {
        clusters = new HashSet<>();
    }

    private @NotNull ClientCluster getClientCluster(@NotNull String clearClusterName) {
        return new HashSetClientCluster(clearClusterName);
    }

    @Override
    public Cluster get(@NotNull String clusterID) {
        return getOptionalCluster(clusterID).orElseGet(() -> {
            Cluster cluster = getClientCluster(clusterID.substring(1));
            clusters.add(cluster);
            return cluster;
        });
    }

    public void add(Cluster cluster) {
        clusters.add(cluster);
    }

    private Optional<Cluster> getOptionalCluster(@NotNull String clusterID) {
        return clusters.stream().filter(cluster -> cluster.getID().equals(clusterID)).findFirst();
    }

    @Override
    public void removeSession(@NotNull Session session) {
        clusters.forEach(session::removeFromCluster);
    }
}
