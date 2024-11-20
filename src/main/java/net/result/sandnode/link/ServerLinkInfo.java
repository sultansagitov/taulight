package net.result.sandnode.link;

import net.result.sandnode.util.Endpoint;
import net.result.sandnode.util.encryption.interfaces.IKeyStorage;

public interface ServerLinkInfo extends LinkInfo {
    Endpoint getEndpoint();

    IKeyStorage getKeyStorage();
}
