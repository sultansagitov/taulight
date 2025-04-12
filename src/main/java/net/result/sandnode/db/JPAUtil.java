package net.result.sandnode.db;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class JPAUtil {
    public static final String persistenceUnitName = "taulight-unit";

    private static EntityManagerFactory emf = null;
    private static EntityManager em = null;

    public static void buildEntityManagerFactory() {
        if (emf != null && emf.isOpen()) return;

        try {
            emf = Persistence.createEntityManagerFactory(persistenceUnitName);
        } catch (Exception ex) {
            System.err.println("Initial EntityManagerFactory creation failed: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static EntityManager getEntityManager() {
        if (em == null) em = emf.createEntityManager();
        return em;
    }

    public static void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}

