package net.result.sandnode.db;

import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.util.Container;
import net.result.sandnode.util.JPAUtil;

public class EncryptedKeyRepository {
    private final JPAUtil jpaUtil;

    public EncryptedKeyRepository(Container container) {
        jpaUtil = container.get(JPAUtil.class);
    }

    public EncryptedKeyEntity create(MemberEntity sender, MemberEntity receiver, String encrypted) throws DatabaseException {
        return jpaUtil.create(new EncryptedKeyEntity(sender, receiver, encrypted));
    }
}
