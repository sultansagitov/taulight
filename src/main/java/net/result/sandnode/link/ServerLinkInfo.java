package net.result.sandnode.link;

import net.result.sandnode.util.Endpoint;
import net.result.sandnode.encryption.interfaces.IAsymmetricKeyStorage;

public interface ServerLinkInfo extends LinkInfo {
    Endpoint endpoint();

    IAsymmetricKeyStorage keyStorage();
}
