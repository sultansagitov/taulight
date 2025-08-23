package net.result.taulight.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.util.Container;
import net.result.sandnode.db.JPAUtil;
import net.result.taulight.entity.MessageEntity;
import net.result.taulight.entity.ReactionEntryEntity;
import net.result.taulight.entity.ReactionTypeEntity;
import net.result.taulight.entity.TauMemberEntity;

import java.util.Optional;

public class ReactionEntryRepository {
    private final JPAUtil jpaUtil;

    public ReactionEntryRepository(Container container) {
        jpaUtil = container.get(JPAUtil.class);
    }

    public ReactionEntryEntity create(TauMemberEntity member, MessageEntity message, ReactionTypeEntity reactionType) {
        ReactionEntryEntity managed = jpaUtil.create(new ReactionEntryEntity(member, message, reactionType));

        member.getReactionEntries().add(managed);
        message.getReactionEntries().add(managed);
        reactionType.getReactionEntries().add(managed);

        return managed;
    }

    public boolean delete(ReactionEntryEntity reactionEntry) {
        EntityManager em = jpaUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            ReactionEntryEntity re = em.find(ReactionEntryEntity.class, reactionEntry.id());
            if (re != null) {
                transaction.begin();

                MessageEntity message = re.getMessage();
                message.getReactionEntries().remove(re);
                em.merge(message);

                TauMemberEntity member = re.getMember();
                member.getReactionEntries().remove(re);
                em.merge(member);

                ReactionTypeEntity type = re.getReactionType();
                type.getReactionEntries().remove(re);
                em.merge(type);

                em.remove(re);
                transaction.commit();
                return true;
            }
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }

        return false;
    }

    public boolean delete(MessageEntity message, TauMemberEntity member, ReactionTypeEntity reactionType) {
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
