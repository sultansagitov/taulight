package net.result.taulight.db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.util.Container;
import net.result.sandnode.util.JPAUtil;

import java.util.Optional;

public class ReactionPackageRepository {
    private final JPAUtil jpaUtil;

    public ReactionPackageRepository(Container container) {
        jpaUtil = container.get(JPAUtil.class);
    }

    private ReactionPackageEntity save(ReactionPackageEntity packageEntity) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();

        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            ReactionPackageEntity managed = em.merge(packageEntity);
            transaction.commit();
            return managed;
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }
    }

    public ReactionPackageEntity create(String name, String description) throws DatabaseException {
        return save(new ReactionPackageEntity(name, description));
    }

    public Optional<ReactionPackageEntity> find(String packageName) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        try {
            String q = "FROM ReactionPackageEntity WHERE name = :name";
            return em.createQuery(q, ReactionPackageEntity.class)
                    .setParameter("name", packageName)
                    .setMaxResults(1)
                    .getResultList().stream()
                    .findFirst();
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }
}
