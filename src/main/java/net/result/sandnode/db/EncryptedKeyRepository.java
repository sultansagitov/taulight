package net.result.sandnode.db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
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

    private EncryptedKeyEntity save(EncryptedKeyEntity ek) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        while (em.find(EncryptedKeyEntity.class, ek.id()) != null) {
            ek.setRandomID();
        }

        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            EncryptedKeyEntity merge = em.merge(ek);
            transaction.commit();

            return merge;
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }
    }

    public EncryptedKeyEntity create(
            MemberEntity sender,
            MemberEntity receiver,
            KeyStorageEntity encryptor,
            String encrypted
    ) throws DatabaseException {
        return save(new EncryptedKeyEntity(sender, receiver, encryptor, encrypted));
    }

    public Optional<EncryptedKeyEntity> find(UUID keyID) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        try {
             return Optional.ofNullable(em.find(EncryptedKeyEntity.class, keyID));
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }
}
