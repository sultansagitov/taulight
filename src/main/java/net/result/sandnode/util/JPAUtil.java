package net.result.sandnode.util;

import net.result.sandnode.exception.ConfigurationException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class JPAUtil {
    public static final String persistenceUnitName = "taulight-unit";

    private final EntityManagerFactory emf;
    private final EntityManager em;

    public JPAUtil(Container container) throws ConfigurationException {
        try {
            emf = Persistence.createEntityManagerFactory(persistenceUnitName);
            em = emf.createEntityManager();
        } catch (Exception e) {
            throw new ConfigurationException("Initial EntityManagerFactory creation failed", e);
        }
    }

    public EntityManager getEntityManager() {
        return em;
    }

    public void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}

