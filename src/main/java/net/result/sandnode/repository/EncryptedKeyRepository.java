package net.result.sandnode.repository;

import net.result.sandnode.entity.EncryptedKeyEntity;
import net.result.sandnode.entity.MemberEntity;
import net.result.sandnode.util.Container;
import net.result.sandnode.db.JPAUtil;

public class EncryptedKeyRepository {
    private final JPAUtil jpaUtil;

    public EncryptedKeyRepository(Container container) {
        jpaUtil = container.get(JPAUtil.class);
    }

    public EncryptedKeyEntity create(String encrypted, MemberEntity sender, MemberEntity receiver) {
        return jpaUtil.create(new EncryptedKeyEntity(encrypted, sender, receiver));
    }
}
