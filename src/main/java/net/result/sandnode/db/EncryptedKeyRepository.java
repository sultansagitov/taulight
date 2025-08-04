package net.result.sandnode.db;

import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.util.Container;
import net.result.sandnode.util.JPAUtil;

import java.util.Optional;
import java.util.UUID;

public class EncryptedKeyRepository {
    private final JPAUtil jpaUtil;

    public EncryptedKeyRepository(Container container) {
        jpaUtil = container.get(JPAUtil.class);
    }

    public Optional<EncryptedKeyEntity> find(UUID keyID) throws DatabaseException {
        return jpaUtil.find(EncryptedKeyEntity.class, keyID);
    }

    public EncryptedKeyEntity create(MemberEntity sender, MemberEntity receiver, String encrypted) throws DatabaseException {
        return jpaUtil.create(new EncryptedKeyEntity(sender, receiver, encrypted));
    }
}
