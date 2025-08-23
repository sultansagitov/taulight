package net.result.taulight.repository;

import jakarta.persistence.EntityManager;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.util.Container;
import net.result.sandnode.db.JPAUtil;
import net.result.taulight.entity.ReactionPackageEntity;

import java.util.Optional;

public class ReactionPackageRepository {
    private final JPAUtil jpaUtil;

    public ReactionPackageRepository(Container container) {
        jpaUtil = container.get(JPAUtil.class);
    }

    public ReactionPackageEntity create(String name, String description) {
        return jpaUtil.create(new ReactionPackageEntity(name, description));
    }

    public Optional<ReactionPackageEntity> find(String packageName) {
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
