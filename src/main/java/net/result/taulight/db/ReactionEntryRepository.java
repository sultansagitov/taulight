package net.result.taulight.db;

import net.result.sandnode.db.JPAUtil;
import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.exception.DatabaseException;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

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

    public boolean delete(ReactionEntryEntity reactionEntry) throws DatabaseException {
        try {
            if (em.contains(reactionEntry)) {
                em.remove(reactionEntry);
                return true;
            }
        } catch (Exception e) {
            throw new DatabaseException("Failed to delete reaction entry", e);
        }
        
        return false;
    }

    public Collection<ReactionEntryEntity> find(
            MessageEntity message,
            MemberEntity member,
            ReactionTypeEntity reactionType
    ) {
        String queryStr = """
            SELECT r FROM ReactionEntryEntity r
            WHERE
                r.message = :message
                AND r.member = :member
                AND r.reactionType = :reactionType
        """;
        TypedQuery<ReactionEntryEntity> query = em.createQuery(queryStr, ReactionEntryEntity.class);
        query.setParameter("message", message);
        query.setParameter("member", member);
        query.setParameter("reactionType", reactionType);

        return query.getResultList();
    }

    public Optional<ReactionEntryEntity> findFirst(
            MessageEntity message,
            MemberEntity member,
            ReactionTypeEntity reactionType
    ) {
        String queryStr = """
            SELECT r FROM ReactionEntryEntity r
            WHERE
                r.message = :message
                AND r.member = :member
                AND r.reactionType = :reactionType
        """;
        TypedQuery<ReactionEntryEntity> query = em.createQuery(queryStr, ReactionEntryEntity.class);
        query.setParameter("message", message);
        query.setParameter("member", member);
        query.setParameter("reactionType", reactionType);
        query.setMaxResults(1);

        List<ReactionEntryEntity> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}
