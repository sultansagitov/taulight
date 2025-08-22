package net.result.taulight.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import net.result.sandnode.entity.FileEntity;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.util.Container;
import net.result.sandnode.db.JPAUtil;
import net.result.taulight.entity.ChatEntity;
import net.result.taulight.entity.MessageEntity;
import net.result.taulight.entity.MessageFileEntity;
import net.result.taulight.entity.TauMemberEntity;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public class MessageFileRepository {
    private final JPAUtil jpaUtil;

    public MessageFileRepository(Container container) {
        jpaUtil = container.get(JPAUtil.class);
    }

    public MessageFileEntity create(TauMemberEntity member, ChatEntity chat, String originalName, FileEntity file) {
        return jpaUtil.create(new MessageFileEntity(member, chat, originalName, file));
    }

    public Collection<MessageFileEntity> getFiles(MessageEntity message) {
        EntityManager em = jpaUtil.getEntityManager();
        try {
            String q = "SELECT f FROM MessageFileEntity f WHERE f.message = :message";
            TypedQuery<MessageFileEntity> query = em.createQuery(q, MessageFileEntity.class);
            query.setParameter("message", message);
            return query.getResultList();
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public void setMessage(MessageEntity message, Set<UUID> fileIDs) {
        if (fileIDs == null || fileIDs.isEmpty()) return;

        EntityManager em = jpaUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();

            String q = "SELECT f FROM MessageFileEntity f WHERE f.id IN :ids";
            TypedQuery<MessageFileEntity> query = em.createQuery(q, MessageFileEntity.class);
            query.setParameter("ids", fileIDs);
            for (MessageFileEntity file : query.getResultList()) {
                if (!file.member().equals(message.getMember()) || file.message() != null) {
                    throw new UnauthorizedException();
                }

                file.setMessage(message);
            }

            transaction.commit();
        } catch (UnauthorizedException e) {
            if (transaction.isActive()) transaction.rollback();
            throw e;
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }
    }
}
