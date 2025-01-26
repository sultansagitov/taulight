package net.result.sandnode.link;

import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.util.Endpoint;

public record SandnodeLinkRecord(Endpoint endpoint, AsymmetricKeyStorage keyStorage) {
}

