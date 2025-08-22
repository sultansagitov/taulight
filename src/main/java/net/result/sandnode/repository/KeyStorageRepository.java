package net.result.sandnode.repository;

import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.entity.KeyStorageEntity;
import net.result.sandnode.util.Container;
import net.result.sandnode.db.JPAUtil;

public class KeyStorageRepository {
    private final JPAUtil jpaUtil;

    @SuppressWarnings("unused")
    public KeyStorageRepository(Container container) {
        this.jpaUtil = container.get(JPAUtil.class);
    }

    public KeyStorageEntity create(AsymmetricKeyStorage keyStorage) {
        return jpaUtil.create(new KeyStorageEntity(keyStorage.encryption(), keyStorage.encodedPublicKey()));
    }
}
