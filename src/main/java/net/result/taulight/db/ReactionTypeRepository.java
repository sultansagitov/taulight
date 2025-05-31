package net.result.taulight.db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.util.Container;
import net.result.sandnode.util.JPAUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ReactionTypeRepository {
    private final JPAUtil jpaUtil;

    public ReactionTypeRepository(Container container) {
        jpaUtil = container.get(JPAUtil.class);
    }

    private ReactionTypeEntity save(@NotNull ReactionTypeEntity reactionType) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        while (em.find(ReactionTypeEntity.class, reactionType.id()) != null) {
            reactionType.setRandomID();
        }

        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            ReactionTypeEntity managed = em.merge(reactionType);
            transaction.commit();
            return managed;
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }
    }

    public ReactionTypeEntity create(String name, ReactionPackageEntity reactionPackage) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        ReactionTypeEntity managed = save(new ReactionTypeEntity(name, reactionPackage));

        reactionPackage.reactionTypes().add(managed);
        em.merge(reactionPackage);

        return managed;
    }

    public Collection<ReactionTypeEntity> create(ReactionPackageEntity rp, Collection<String> types)
            throws DatabaseException {
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

                // Ensure unique ID
                while (em.find(ReactionTypeEntity.class, reactionType.id()) != null) {
                    reactionType.setRandomID();
                }

                ReactionTypeEntity managed = em.merge(reactionType);
                createdEntities.add(managed);

                rp.reactionTypes().add(managed);
                em.merge(rp);
            }

            transaction.commit();
            return createdEntities;
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }
    }

    public List<ReactionTypeEntity> findByPackageName(String packageName) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        try {
            String q = "FROM ReactionTypeEntity WHERE reactionPackage.name = :packageName";
            return em.createQuery(q, ReactionTypeEntity.class).setParameter("packageName", packageName).getResultList();
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }
}
