package net.result.taulight.db;

import net.result.sandnode.db.JPAUtil;
import net.result.sandnode.exception.DatabaseException;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

public class ReactionEntryRepository {
    private final EntityManager em = JPAUtil.getEntityManager();

    public void save(ReactionEntryEntity reactionEntry) throws DatabaseException {
        while (em.contains(reactionEntry)) {
            reactionEntry.setRandomID();
        }

        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(reactionEntry);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw new DatabaseException("Failed to save reaction entry", e);
        }
    }

    public void delete(ReactionEntryEntity reactionEntry) throws DatabaseException {
        try {
            em.remove(em.contains(reactionEntry) ? reactionEntry : em.merge(reactionEntry));
        } catch (Exception e) {
            throw new DatabaseException("Failed to delete reaction entry", e);
        }
    }
}
