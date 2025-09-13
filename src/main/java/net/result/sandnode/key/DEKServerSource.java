package net.result.sandnode.key;

import net.result.sandnode.serverclient.SandnodeClient;
import net.result.sandnode.util.Address;
import net.result.sandnode.util.Member;

public class DEKServerSource extends ServerSource {
    public final Member personalKeyOf;

    public DEKServerSource(Address address, Member personalKeyOf) {
        super(address);
        this.personalKeyOf = personalKeyOf;
    }

    public DEKServerSource(SandnodeClient client) {
        this(client.address, new Member(client));
    }
}
