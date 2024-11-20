package net.result.sandnode.link;

import net.result.sandnode.util.Endpoint;
import net.result.sandnode.util.encryption.interfaces.IKeyStorage;

public class MemberInfo {
    public final Endpoint endpoint;
    public final String userId;
    public final IKeyStorage keyStorage;

    public MemberInfo(Endpoint endpoint, String userId, IKeyStorage keyStorage) {
        this.endpoint = endpoint;
        this.userId = userId;
        this.keyStorage = keyStorage;
    }
}
