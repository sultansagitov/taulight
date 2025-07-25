package net.result.sandnode.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import net.result.sandnode.db.BaseEntity;
import net.result.sandnode.exception.ConfigurationException;

public class JPAUtil {
    public static final String persistenceUnitName = "taulight-unit";

    private final EntityManagerFactory emf;

    public JPAUtil(@SuppressWarnings("unused") Container container) throws ConfigurationException {
        try {
            emf = Persistence.createEntityManagerFactory(persistenceUnitName);
        } catch (Exception e) {
            throw new ConfigurationException("Initial EntityManagerFactory creation failed", e);
        }
    }

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends BaseEntity> T refresh(T entity) {
        return getEntityManager().find((Class<T>) entity.getClass(), entity.id());
    }
}
