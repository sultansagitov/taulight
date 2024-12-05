package net.result.sandnode.link;

import net.result.sandnode.util.Endpoint;
import net.result.sandnode.encryption.interfaces.IKeyStorage;

public record MemberInfo(Endpoint endpoint, String memberID, IKeyStorage keyStorage) {
}
