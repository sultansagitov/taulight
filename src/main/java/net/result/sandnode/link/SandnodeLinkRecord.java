package net.result.sandnode.link;

import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.message.util.NodeType;
import net.result.sandnode.util.Endpoint;

public record SandnodeLinkRecord(NodeType nodeType, Endpoint endpoint, AsymmetricKeyStorage keyStorage) {
}

