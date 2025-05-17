package net.result.taulight.db;

import net.result.sandnode.util.JPAUtil;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.util.Container;
import org.jetbrains.annotations.NotNull;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.Optional;

public class ReactionEntryRepository {
    private final JPAUtil jpaUtil;

    public ReactionEntryRepository(Container container) {
        jpaUtil = container.get(JPAUtil.class);
    }

    private ReactionEntryEntity save(@NotNull ReactionEntryEntity reactionEntry) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
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
        EntityManager em = jpaUtil.getEntityManager();
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
        EntityManager em = jpaUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            ReactionEntryEntity re = em.find(ReactionEntryEntity.class, reactionEntry.id());
            if (re != null) {
                transaction.begin();

                MessageEntity message = re.message();
                message.reactionEntries().remove(re);
                em.merge(message);

                TauMemberEntity member = re.member();
                member.reactionEntries().remove(re);
                em.merge(member);

                ReactionTypeEntity type = re.reactionType();
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

    public boolean delete(MessageEntity message, TauMemberEntity member, ReactionTypeEntity reactionType)
            throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
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
