package net.result.taulight.db;

import net.result.sandnode.db.JPAUtil;
import net.result.sandnode.exception.DatabaseException;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;

public class ReactionTypeRepository {
    private final EntityManager em = JPAUtil.getEntityManager();

    public void save(ReactionTypeEntity reactionType) throws DatabaseException {
        while (em.contains(reactionType)) {
            reactionType.setRandomID();
        }

        EntityTransaction transaction = em.getTransaction();
        try {
            reactionType.setRandomID();
            transaction.begin();
            em.merge(reactionType);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw new DatabaseException("Failed to save reaction type", e);
        }
    }

    public List<ReactionTypeEntity> findByPackageName(String packageName) throws DatabaseException {
        try {
            String q = "SELECT r FROM ReactionTypeEntity r WHERE r.packageName = :packageName";
            return em.createQuery(q, ReactionTypeEntity.class).setParameter("packageName", packageName).getResultList();
        } catch (Exception e) {
            throw new DatabaseException("Failed to find reaction types by package name", e);
        }
    }
}
