package net.result.sandnode.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.UnknownSandnodeErrorException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.types.ClusterRequest;
import net.result.sandnode.message.types.ClusterResponse;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.serverclient.SandnodeClient;

import java.util.Collection;

public class ClusterClientChain extends ClientChain {
    public ClusterClientChain(SandnodeClient client) {
        super(client);
    }

    public Collection<String> remove(Collection<String> clusters) throws InterruptedException, ExpectedMessageException,
            UnprocessedMessagesException, UnknownSandnodeErrorException, SandnodeErrorException {
        ClusterRequest request = new ClusterRequest(new Headers().setValue("mode", "remove"), clusters);
        var raw = sendAndReceive(request);
        return new ClusterResponse(raw).getClustersID();
    }

    public Collection<String> add(Collection<String> clusters) throws InterruptedException, ExpectedMessageException,
            UnprocessedMessagesException, UnknownSandnodeErrorException, SandnodeErrorException {
        ClusterRequest request = new ClusterRequest(new Headers().setValue("mode", "add"), clusters);
        var raw = sendAndReceive(request);
        return new ClusterResponse(raw).getClustersID();
    }
}
