package net.result.sandnode.key;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.sandnode.util.Address;
import net.result.sandnode.util.Member;

public class DEKServerSource extends ServerSource {
    @JsonProperty
    public Member personalKeyOwner;

    @SuppressWarnings("unused")
    public DEKServerSource() {}

    public DEKServerSource(Address address, Member personalKeyOwner) {
        super(address);
        this.personalKeyOwner = personalKeyOwner;
    }

    public DEKServerSource(SandnodeClient client) {
        this(client.address, new Member(client));
    }
}
