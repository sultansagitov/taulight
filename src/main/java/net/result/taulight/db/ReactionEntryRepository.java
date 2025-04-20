package net.result.taulight.db;

import net.result.sandnode.db.JPAUtil;
import net.result.sandnode.exception.DatabaseException;
import org.jetbrains.annotations.NotNull;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.Optional;

public class ReactionEntryRepository {
    private final EntityManager em = JPAUtil.getEntityManager();

    private ReactionEntryEntity save(@NotNull ReactionEntryEntity reactionEntry) throws DatabaseException {
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

    public ReactionEntryEntity create(TauMemberEntity member, MessageEntity message, ReactionTypeEntity reactionType)
            throws DatabaseException {
        ReactionEntryEntity managed = save(new ReactionEntryEntity(member, message, reactionType));

        member.reactionEntries().add(managed);
        em.merge(member);

        message.reactionEntries().add(managed);
        em.merge(message);

        reactionType.reactionEntries().add(managed);
        em.merge(reactionType);

        return managed;
    }

    public boolean delete(ReactionEntryEntity reactionEntry) throws DatabaseException {
        EntityTransaction transaction = em.getTransaction();
        try {
            ReactionEntryEntity re = em.find(ReactionEntryEntity.class, reactionEntry.id());
            if (re != null) {
                transaction.begin();

                MessageEntity message = reactionEntry.message();
                message.reactionEntries().remove(re);
                em.merge(message);

                TauMemberEntity member = reactionEntry.member();
                member.reactionEntries().remove(re);
                em.merge(member);

                ReactionTypeEntity type = reactionEntry.reactionType();
                type.reactionEntries().remove(re);
                em.merge(type);

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

    public boolean removeReactionEntry(MessageEntity message, TauMemberEntity member, ReactionTypeEntity reactionType)
            throws DatabaseException {
        String q = """
            FROM ReactionEntryEntity r WHERE
                r.message = :message
                AND r.member = :member
                AND r.reactionType = :reactionType
        """;
        Optional<ReactionEntryEntity> reactionEntry = em.createQuery(q, ReactionEntryEntity.class)
                .setParameter("message", message)
                .setParameter("member", member)
                .setParameter("reactionType", reactionType)
                .setMaxResults(1)
                .getResultList()
                .stream()
                .findFirst();

        if (reactionEntry.isPresent()) {
            delete(reactionEntry.get());
            return true;
        }

        return false;
    }
}
