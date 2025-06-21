package net.result.sandnode.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.cluster.Cluster;
import net.result.sandnode.message.types.ClusterRequest;
import net.result.sandnode.message.types.ClusterResponse;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.cluster.ClusterManager;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ClusterServerChain extends ServerChain implements ReceiverChain {
    public ClusterServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException, ExpectedMessageException, UnprocessedMessagesException {
        ClusterManager clusterManager = session.server.container.get(ClusterManager.class);
        ClusterRequest request = new ClusterRequest(queue.take());

        Set<Cluster> clusters = request
                .getClustersID().stream()
                .map(clusterManager::get)
                .collect(Collectors.toSet());

        Optional<String> opt = request.headers().getOptionalValue("mode");
        boolean add = opt.isEmpty() || !opt.get().equals("remove");

        if (add) session.addToClusters(clusters);
        else session.removeFromClusters(clusters);

        sendFin(new ClusterResponse(session
                .getClusters().stream()
                .map(Cluster::getID)
                .collect(Collectors.toSet())
        ));
    }
}
