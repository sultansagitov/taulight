package net.result.sandnode.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import net.result.sandnode.db.BaseEntity;
import net.result.sandnode.exception.ConfigurationException;
import net.result.sandnode.exception.DatabaseException;

import java.util.Optional;
import java.util.UUID;

public class SimpleJPAUtil implements JPAUtil {
    public static final String persistenceUnitName = "taulight-unit";

    private final EntityManagerFactory emf;

    public SimpleJPAUtil(@SuppressWarnings("unused") Container container) throws ConfigurationException {
        try {
            emf = Persistence.createEntityManagerFactory(persistenceUnitName);
        } catch (Exception e) {
            throw new ConfigurationException("Initial EntityManagerFactory creation failed", e);
        }
    }

    @Override
    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    @Override
    public void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends BaseEntity> T refresh(T entity) throws DatabaseException {
        return find((Class<T>) entity.getClass(), entity.id()).orElseThrow();
    }

    @Override
    public <T extends BaseEntity> T create(T entity) throws DatabaseException {
        EntityManager em = getEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();
            T managed = em.merge(entity);
            transaction.commit();

            return managed;
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }
    }

    @Override
    public <T extends BaseEntity> Optional<T> find(Class<T> clazz, UUID id) throws DatabaseException {
        EntityManager em = getEntityManager();
        try {
            return Optional.ofNullable(em.find(clazz, id));
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }
}
