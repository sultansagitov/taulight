package net.result.taulight.db;

import net.result.sandnode.db.JPAUtil;
import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.exception.DatabaseException;
import org.jetbrains.annotations.NotNull;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

public class ReactionEntryRepository {
    private final EntityManager em = JPAUtil.getEntityManager();

    public ReactionEntryEntity save(@NotNull ReactionEntryEntity reactionEntry) throws DatabaseException {
        while (em.find(ReactionEntryEntity.class, reactionEntry.id()) != null) {
            reactionEntry.setRandomID();
        }

        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            ReactionEntryEntity managed = em.merge(reactionEntry);
            transaction.commit();
            return managed;
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException("Failed to save reaction entry", e);
        }
    }

    public ReactionEntryEntity create(MemberEntity member, MessageEntity message, ReactionTypeEntity reactionType)
            throws DatabaseException {
        return save(new ReactionEntryEntity(member, message, reactionType));
    }

    public boolean delete(ReactionEntryEntity reactionEntry) throws DatabaseException {
        EntityTransaction transaction = em.getTransaction();
        try {
            ReactionEntryEntity re = em.find(ReactionEntryEntity.class, reactionEntry.id());
            if (re != null) {
                transaction.begin();
                em.remove(re);
                transaction.commit();
                return true;
            }
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException("Failed to delete reaction entry", e);
        }

        return false;
    }
}
