package net.result.taulight.db;

import net.result.sandnode.db.JPAUtil;
import net.result.sandnode.exception.DatabaseException;
import net.result.taulight.dto.ChatMessageViewDTO;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MessageRepository {
    private final EntityManager em = JPAUtil.getEntityManager();

    public void save(MessageEntity message) throws DatabaseException {
        while (em.contains(message)) {
            message.setRandomID();
        }

        String q = "SELECT m FROM MessageEntity m WHERE m.id = :id";
        while (true) {
            TypedQuery<MessageEntity> query = em.createQuery(q, MessageEntity.class);
            query.setParameter("id", message.id());
            List<MessageEntity> resultList = query.getResultList();
            if (resultList.isEmpty()) break;
        }

        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(message);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
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

    public List<ChatMessageViewDTO> findMessagesByChat(ChatEntity chat, int index, int size) throws DatabaseException {
        try {
            String q = """
                SELECT NEW net.result.taulight.dto.ChatMessageViewDTO(m)
                FROM MessageEntity m
                WHERE m.chat = :chat ORDER BY m.timestamp DESC
            """;
            return em.createQuery(q, ChatMessageViewDTO.class)
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
