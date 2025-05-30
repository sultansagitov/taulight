package net.result.taulight.db;

import net.result.sandnode.util.JPAUtil;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.util.Container;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
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
        while (em.find(InviteCodeEntity.class, code.id()) != null) code.setRandomID();

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
            ChannelEntity channel,
            TauMemberEntity receiver,
            TauMemberEntity sender,
            ZonedDateTime expiresDate
    ) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        InviteCodeEntity managed = save(new InviteCodeEntity(channel, receiver, sender, expiresDate));

        channel.inviteCodes().add(managed);
        em.merge(channel);

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

    public Collection<InviteCodeEntity> find(ChannelEntity channel, TauMemberEntity receiver)
            throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        try {
            String q = "FROM InviteCodeEntity WHERE channel = :channel AND receiver = :receiver";
            return em.createQuery(q, InviteCodeEntity.class)
                    .setParameter("channel", channel)
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
