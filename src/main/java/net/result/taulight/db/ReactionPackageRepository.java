package net.result.taulight.db;

import net.result.sandnode.db.JPAUtil;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.util.Container;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.Optional;

public class ReactionPackageRepository {
    private final EntityManager em = JPAUtil.getEntityManager();

    public ReactionPackageRepository(Container container) {}

    private ReactionPackageEntity save(ReactionPackageEntity packageEntity) throws DatabaseException {
        while (em.find(ReactionPackageEntity.class, packageEntity.id()) != null) {
            packageEntity.setRandomID();
        }

        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            ReactionPackageEntity managed = em.merge(packageEntity);
            transaction.commit();
            return managed;
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException("Failed to save reaction package", e);
        }
    }

    public ReactionPackageEntity create(String name, String description) throws DatabaseException {
        return save(new ReactionPackageEntity(name, description));
    }

    public Optional<ReactionPackageEntity> find(String packageName) throws DatabaseException {
        try {
            String q = "FROM ReactionPackageEntity WHERE name = :name";
            return em.createQuery(q, ReactionPackageEntity.class)
                    .setParameter("name", packageName)
                    .setMaxResults(1)
                    .getResultList().stream()
                    .findFirst();
        } catch (Exception e) {
            throw new DatabaseException("Failed to retrieve reaction packages", e);
        }
    }
}
