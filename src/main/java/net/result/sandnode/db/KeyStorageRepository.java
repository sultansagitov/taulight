package net.result.sandnode.db;

import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.crypto.CannotUseEncryption;
import net.result.sandnode.util.Container;
import net.result.sandnode.util.JPAUtil;

public class KeyStorageRepository {
    private final JPAUtil jpaUtil;

    @SuppressWarnings("unused")
    public KeyStorageRepository(Container container) {
        this.jpaUtil = container.get(JPAUtil.class);
    }

    public KeyStorageEntity create(AsymmetricKeyStorage keyStorage) throws CannotUseEncryption, DatabaseException {
        return jpaUtil.create(new KeyStorageEntity(keyStorage.encryption(), keyStorage.encodedPublicKey()));
    }
}
