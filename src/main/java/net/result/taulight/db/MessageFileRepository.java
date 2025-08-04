package net.result.taulight.db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import net.result.sandnode.db.FileEntity;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.util.Container;
import net.result.sandnode.util.JPAUtil;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public class MessageFileRepository {
    private final JPAUtil jpaUtil;

    public MessageFileRepository(Container container) {
        jpaUtil = container.get(JPAUtil.class);
    }

    public MessageFileEntity create(TauMemberEntity member, ChatEntity chat, String originalName, FileEntity file)
            throws DatabaseException {
        return jpaUtil.create(new MessageFileEntity(member, chat, originalName, file));
    }

    public Collection<MessageFileEntity> getFiles(MessageEntity message) throws DatabaseException {
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

    public void setMessage(MessageEntity message, Set<UUID> fileIDs) throws DatabaseException, UnauthorizedException {
        if (fileIDs == null || fileIDs.isEmpty()) return;

        EntityManager em = jpaUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();

            String q = "SELECT f FROM MessageFileEntity f WHERE f.id IN :ids";
            TypedQuery<MessageFileEntity> query = em.createQuery(q, MessageFileEntity.class);
            query.setParameter("ids", fileIDs);
            for (MessageFileEntity file : query.getResultList()) {
                if (!file.member().equals(message.member()) || file.message() != null) {
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
