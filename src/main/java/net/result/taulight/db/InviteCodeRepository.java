package net.result.taulight.db;

import net.result.sandnode.db.JPAUtil;
import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.exception.DatabaseException;
import org.jetbrains.annotations.NotNull;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public class InviteCodeRepository {
    private final EntityManager em = JPAUtil.getEntityManager();

    public void save(InviteCodeEntity code) throws DatabaseException {
        while (em.contains(code)) {
            code.setRandomID();
        }

        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(code);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw new DatabaseException(e);
        }
    }

    public Optional<InviteCodeEntity> findByCode(String code) throws DatabaseException {
        try {
            String q = "SELECT i FROM InviteCodeEntity i WHERE i.code = :code";
            return em.createQuery(q, InviteCodeEntity.class)
                    .setParameter("code", code)
                    .setMaxResults(1)
                    .getResultList()
                    .stream().findFirst();
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public void delete(@NotNull InviteCodeEntity inviteCodeEntity) throws DatabaseException {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.remove(em.contains(inviteCodeEntity) ? inviteCodeEntity : em.merge(inviteCodeEntity));
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw new DatabaseException("Failed to delete invite code", e);
        }
    }

    public void activate(InviteCodeEntity code) throws DatabaseException {
        EntityTransaction transaction = em.getTransaction();
        try {
            code.setActivationDateNow();
            transaction.begin();
            em.merge(code);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw new DatabaseException("Failed to activate invite code", e);
        }
    }

    public List<InviteCodeEntity> findBySender(MemberEntity sender, boolean includeExpired, boolean includeActivated) throws DatabaseException {
        try {
            StringBuilder queryBuilder = new StringBuilder("SELECT i FROM InviteCodeEntity i WHERE i.sender = :sender");

            if (!includeExpired) {
                queryBuilder.append(" AND (i.expirationDate IS NULL OR i.expirationDate > :now)");
            }

            if (!includeActivated) {
                queryBuilder.append(" AND i.activationDate IS NULL");
            }

            TypedQuery<InviteCodeEntity> query = em
                    .createQuery(queryBuilder.toString(), InviteCodeEntity.class)
                    .setParameter("sender", sender);

            if (!includeExpired) {
                query.setParameter("now", ZonedDateTime.now(ZoneId.of("UTC")));
            }

            return query.getResultList();
        } catch (Exception e) {
            throw new DatabaseException("Failed to retrieve invite codes by sender", e);
        }
    }
}
