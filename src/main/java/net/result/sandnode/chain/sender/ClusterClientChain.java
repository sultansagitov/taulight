package net.result.sandnode.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.message.types.ClusterRequest;
import net.result.sandnode.message.types.ClusterResponse;
import net.result.sandnode.serverclient.SandnodeClient;

import java.util.Collection;

public class ClusterClientChain extends ClientChain {
    public ClusterClientChain(SandnodeClient client) {
        super(client);
    }

    public Collection<String> remove(Collection<String> clusters)
            throws InterruptedException, ExpectedMessageException, UnprocessedMessagesException {
        ClusterRequest request = new ClusterRequest(clusters);
        request.headers().setValue("mode", "remove");
        send(request);
        return new ClusterResponse(queue.take()).getClustersID();
    }

    public Collection<String> add(Collection<String> clusters)
            throws InterruptedException, ExpectedMessageException, UnprocessedMessagesException {
        ClusterRequest request = new ClusterRequest(clusters);
        request.headers().setValue("mode", "add");
        send(request);
        return new ClusterResponse(queue.take()).getClustersID();
    }
}
