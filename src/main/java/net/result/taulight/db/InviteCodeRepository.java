package net.result.taulight.db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.util.Container;
import net.result.sandnode.util.JPAUtil;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Optional;

public class InviteCodeRepository {
    private final JPAUtil jpaUtil;

    public InviteCodeRepository(Container container) {
        jpaUtil = container.get(JPAUtil.class);
    }

    private InviteCodeEntity save(InviteCodeEntity code) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();

        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            InviteCodeEntity managed = em.merge(code);
            transaction.commit();
            return managed;
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }
    }

    public InviteCodeEntity create(
            GroupEntity group,
            TauMemberEntity receiver,
            TauMemberEntity sender,
            ZonedDateTime expiresDate
    ) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        InviteCodeEntity managed = save(new InviteCodeEntity(group, receiver, sender, expiresDate));

        group.inviteCodes().add(managed);
        em.merge(group);

        receiver.inviteCodesAsReceiver().add(managed);
        em.merge(receiver);

        sender.inviteCodesAsReceiver().add(managed);
        em.merge(sender);

        return managed;
    }

    public Optional<InviteCodeEntity> find(String code) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        try {
            String q = "FROM InviteCodeEntity WHERE code = :code";
            return em.createQuery(q, InviteCodeEntity.class)
                    .setParameter("code", code)
                    .setMaxResults(1)
                    .getResultList().stream()
                    .findFirst();
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public Collection<InviteCodeEntity> find(GroupEntity group, TauMemberEntity receiver)
            throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        try {
            String q = "FROM InviteCodeEntity WHERE group = :group AND receiver = :receiver";
            return em.createQuery(q, InviteCodeEntity.class)
                    .setParameter("group", group)
                    .setParameter("receiver", receiver)
                    .getResultList();
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public boolean activate(InviteCodeEntity code) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            if (code.activationDate() != null || !code.expiresDate().isAfter(ZonedDateTime.now())) {
                return false;
            }
            code.setActivationDateNow();
            transaction.begin();
            em.merge(code);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }
    }
}
