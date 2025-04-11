package net.result.taulight.db;

import net.result.sandnode.db.JPAUtil;
import net.result.sandnode.exception.DatabaseException;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MessageRepository {
    private final EntityManager em = JPAUtil.getEntityManager();

    public void save(MessageEntity message) throws DatabaseException {
        while (em.find(MessageEntity.class, message.id()) != null) {
            message.setRandomID();
        }

        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(message);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }
    }

    public Optional<MessageEntity> findById(UUID id) throws DatabaseException {
        try {
            return Optional.ofNullable(em.find(MessageEntity.class, id));
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public List<MessageEntity> findMessagesByChat(ChatEntity chat, int index, int size) throws DatabaseException {
        try {
            String q = """
                SELECT m FROM MessageEntity m
                WHERE m.chat = :chat ORDER BY m.creationDate DESC
            """;
            return em.createQuery(q, MessageEntity.class)
                    .setParameter("chat", chat)
                    .setFirstResult(index)
                    .setMaxResults(size)
                    .getResultList();
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public long countMessagesByChat(ChatEntity chat) throws DatabaseException {
        try {
            String q = "SELECT COUNT(m) FROM MessageEntity m WHERE m.chat = :chat";
            return em.createQuery(q, Long.class).setParameter("chat", chat).getSingleResult();
        } catch (Exception e) {
            throw new DatabaseException("Failed to count messages by chat", e);
        }
    }
}
