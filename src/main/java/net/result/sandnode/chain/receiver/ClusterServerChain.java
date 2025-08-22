package net.result.sandnode.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.cluster.Cluster;
import net.result.sandnode.cluster.ClusterManager;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.ClusterRequest;
import net.result.sandnode.message.types.ClusterResponse;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ClusterServerChain extends ServerChain implements ReceiverChain {
    @Override
    public ClusterResponse handle(RawMessage raw) {
        ClusterManager clusterManager = session.server.container.get(ClusterManager.class);
        ClusterRequest request = new ClusterRequest(raw);

        Set<Cluster> clusters = request
                .getClustersID().stream()
                .map(clusterManager::get)
                .collect(Collectors.toSet());

        Optional<String> opt = request.headers().getOptionalValue("mode");
        boolean add = opt.isEmpty() || !opt.get().equals("remove");

        if (add) session.addToClusters(clusters);
        else session.removeFromClusters(clusters);

        Set<String> clusterIDs = session.getClusters().stream().map(Cluster::getID).collect(Collectors.toSet());
        return new ClusterResponse(clusterIDs);
    }
}
