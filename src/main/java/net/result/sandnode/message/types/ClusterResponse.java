package net.result.sandnode.message.types;

import lombok.Getter;
import net.result.sandnode.message.BaseMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;

import java.util.Collection;
import java.util.Set;

@Getter
public class ClusterResponse extends BaseMessage {
    private final Collection<String> clustersID;

    public ClusterResponse(RawMessage raw) {
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

    @Override
    public byte[] getBody() {
        return String.join(",", clustersID).getBytes();
    }
}
