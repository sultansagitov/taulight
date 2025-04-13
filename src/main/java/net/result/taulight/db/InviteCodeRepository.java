package net.result.taulight.db;

import net.result.sandnode.db.JPAUtil;
import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.exception.DatabaseException;
import org.jetbrains.annotations.NotNull;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Optional;

public class InviteCodeRepository {
    private final EntityManager em = JPAUtil.getEntityManager();

    public InviteCodeEntity save(InviteCodeEntity code) throws DatabaseException {
        while (em.find(InviteCodeEntity.class, code.id()) != null) {
            code.setRandomID();
        }

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
            MemberEntity receiver,
            MemberEntity sender,
            ZonedDateTime expiresDate
    ) throws DatabaseException {
        InviteCodeEntity code = new InviteCodeEntity(channel, receiver, sender, expiresDate);
        while (find(code.code()).isPresent()) code.setRandomCode();
        return save(code);
    }

    public Optional<InviteCodeEntity> find(String code) throws DatabaseException {
        try {
            String q = "SELECT i FROM InviteCodeEntity i WHERE i.code = :code";
            return em.createQuery(q, InviteCodeEntity.class)
                    .setParameter("code", code)
                    .setMaxResults(1)
                    .getResultList().stream()
                    .findFirst();
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public Collection<InviteCodeEntity> find(ChannelEntity channel, MemberEntity receiver)
            throws DatabaseException {
        try {
            String q = "SELECT i FROM InviteCodeEntity i WHERE i.channel = :channel AND i.receiver = :receiver";
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
