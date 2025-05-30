package net.result.sandnode.db;

import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.crypto.CannotUseEncryption;
import net.result.sandnode.util.Container;
import net.result.sandnode.util.JPAUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.Optional;
import java.util.UUID;

public class KeyStorageRepository {
    private final JPAUtil jpaUtil;

    @SuppressWarnings("unused")
    public KeyStorageRepository(Container container) {
        this.jpaUtil = container.get(JPAUtil.class);
    }

    public KeyStorageEntity save(KeyStorageEntity entity) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();

        while (em.find(MemberEntity.class, entity.id()) != null) {
            entity.setRandomID();
        }

        try {
            transaction.begin();
            KeyStorageEntity merged = em.merge(entity);
            transaction.commit();
            return merged;
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }
    }

    public KeyStorageEntity create(AsymmetricKeyStorage keyStorage) throws CannotUseEncryption, DatabaseException {
        return save(new KeyStorageEntity(keyStorage.encryption(), keyStorage.encodedPublicKey()));
    }

    public Optional<KeyStorageEntity> find(UUID id) {
        EntityManager em = jpaUtil.getEntityManager();
        return Optional.ofNullable(em.find(KeyStorageEntity.class, id));
    }
}
