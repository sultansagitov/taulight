package net.result.sandnode.link;

import net.result.sandnode.encryption.interfaces.IAsymmetricKeyStorage;
import net.result.sandnode.util.Endpoint;

public record SandnodeLinkRecord(Endpoint endpoint, IAsymmetricKeyStorage keyStorage) {
}

