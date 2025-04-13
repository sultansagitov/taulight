package net.result.taulight.db;

import net.result.sandnode.db.JPAUtil;
import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.exception.DatabaseException;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;
import java.util.Optional;

public class ReactionTypeRepository {
    private final EntityManager em = JPAUtil.getEntityManager();

    public ReactionTypeEntity save(ReactionTypeEntity reactionType) throws DatabaseException {
        while (em.find(ReactionTypeEntity.class, reactionType.id()) != null) {
            reactionType.setRandomID();
        }

        EntityTransaction transaction = em.getTransaction();
        try {
            reactionType.setRandomID();
            transaction.begin();
            ReactionTypeEntity managed = em.merge(reactionType);
            transaction.commit();
            return managed;
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException("Failed to save reaction type", e);
        }
    }

    public ReactionTypeEntity create(String name, String packageName) throws DatabaseException {
        return save(new ReactionTypeEntity(name, packageName));
    }

    public List<ReactionTypeEntity> findByPackageName(String packageName) throws DatabaseException {
        try {
            String q = "SELECT r FROM ReactionTypeEntity r WHERE r.packageName = :packageName";
            return em.createQuery(q, ReactionTypeEntity.class).setParameter("packageName", packageName).getResultList();
        } catch (Exception e) {
            throw new DatabaseException("Failed to find reaction types by package name", e);
        }
    }

    public boolean removeReactionEntry(
            MessageEntity message,
            MemberEntity member,
            ReactionTypeEntity reactionType
    ) throws DatabaseException {
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();

            String q = """
                SELECT r FROM ReactionEntryEntity r
                WHERE
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
                em.remove(reactionEntry.get());
                transaction.commit();
                return true;
            } else {
                if (transaction.isActive()) transaction.rollback();
                return false;
            }

        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException("Failed to remove reaction entry", e);
        }
    }
}
