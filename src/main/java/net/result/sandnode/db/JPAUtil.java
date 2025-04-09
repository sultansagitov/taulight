package net.result.sandnode.db;

public class JPAUtil {
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("example-unit");

    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public static void shutdown() {
        emf.close();
    }
}
