package net.result.taulight.db;

import net.result.sandnode.db.JPAUtil;
import net.result.sandnode.exception.DatabaseException;
import org.jetbrains.annotations.NotNull;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Optional;

public class InviteCodeRepository {
    private final EntityManager em = JPAUtil.getEntityManager();

    private InviteCodeEntity save(InviteCodeEntity code) throws DatabaseException {
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

    public boolean delete(@NotNull InviteCodeEntity inviteCodeEntity) throws DatabaseException {
        EntityTransaction transaction = em.getTransaction();
        try {
            if (em.find(InviteCodeEntity.class, inviteCodeEntity.id()) != null) {
                transaction.begin();
                em.remove(inviteCodeEntity);
                transaction.commit();
                return true;
            }

            return false;
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException("Failed to delete invite code", e);
        }
    }

    public boolean activate(InviteCodeEntity code) throws DatabaseException {
        EntityTransaction transaction = em.getTransaction();
        try {
            if (code.activationDate() != null) {
                return false;
            }
            code.setActivationDateNow();
            transaction.begin();
            em.merge(code);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException("Failed to activate invite code", e);
        }
    }
}
