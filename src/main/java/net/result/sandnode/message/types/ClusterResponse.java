package net.result.sandnode.message.types;

import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;

import java.util.Collection;
import java.util.Set;

public class ClusterResponse extends Message {
    private final Collection<String> clustersID;

    public ClusterResponse(RawMessage raw) throws ExpectedMessageException {
        super(raw.expect(MessageTypes.CLUSTER).headers());
        clustersID = Set.of(new String(raw.getBody()).split(","));
    }

    public ClusterResponse(Collection<String> clustersID) {
        this(new Headers(), clustersID);
    }

    public ClusterResponse(Headers headers, Collection<String> clustersID) {
        super(headers.setType(MessageTypes.CLUSTER));
        this.clustersID = clustersID;
    }

    public Collection<String> getClustersID() {
        return clustersID;
    }

    @Override
    public byte[] getBody() {
        return String.join(",", clustersID).getBytes();
    }
}
