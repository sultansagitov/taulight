package net.result.taulight.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.util.Container;
import net.result.sandnode.db.JPAUtil;
import net.result.taulight.entity.ReactionPackageEntity;
import net.result.taulight.entity.ReactionTypeEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ReactionTypeRepository {
    private final JPAUtil jpaUtil;

    public ReactionTypeRepository(Container container) {
        jpaUtil = container.get(JPAUtil.class);
    }

    public ReactionTypeEntity create(String name, ReactionPackageEntity reactionPackage) {
        EntityManager em = jpaUtil.getEntityManager();
        ReactionTypeEntity managed = jpaUtil.create(new ReactionTypeEntity(name, reactionPackage));

        reactionPackage.getReactionTypes().add(managed);
        em.merge(reactionPackage);

        return managed;
    }

    public Collection<ReactionTypeEntity> create(ReactionPackageEntity rp, Collection<String> types) {
        EntityManager em = jpaUtil.getEntityManager();
        if (types == null || types.isEmpty()) {
            return List.of();
        }

        List<ReactionTypeEntity> createdEntities = new ArrayList<>();
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();

            for (String type : types) {
                ReactionTypeEntity reactionType = new ReactionTypeEntity(type, rp);

                ReactionTypeEntity managed = em.merge(reactionType);
                createdEntities.add(managed);

                rp.getReactionTypes().add(managed);
                em.merge(rp);
            }

            transaction.commit();
            return createdEntities;
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }
    }

    public List<ReactionTypeEntity> findByPackageName(String packageName) {
        EntityManager em = jpaUtil.getEntityManager();
        try {
            String q = "FROM ReactionTypeEntity WHERE reactionPackage.name = :packageName";
            return em.createQuery(q, ReactionTypeEntity.class).setParameter("packageName", packageName).getResultList();
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }
}
