package net.result.taulight.db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.util.Container;
import net.result.sandnode.util.JPAUtil;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class MessageFileRepository {
    private final JPAUtil jpaUtil;

    public MessageFileRepository(Container container) {
        jpaUtil = container.get(JPAUtil.class);
    }

    private MessageFileEntity save(MessageFileEntity file) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        while (em.find(MessageFileEntity.class, file.id()) != null) {
            file.setRandomID();
        }

        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            MessageFileEntity managed = em.merge(file);
            transaction.commit();
            return managed;
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }
    }

    public MessageFileEntity create(TauMemberEntity member, ChatEntity chat, String contentType, String filename)
            throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        MessageFileEntity managed = save(new MessageFileEntity(member, chat, contentType, filename));
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(managed);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }

        return managed;
    }

    public Optional<MessageFileEntity> find(UUID id) throws DatabaseException {
        EntityManager em = jpaUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(MessageFileEntity.class, id));
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
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
