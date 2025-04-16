package net.result.taulight.db;

import net.result.sandnode.db.JPAUtil;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.error.NotFoundException;
import net.result.taulight.dto.ChatMessageInputDTO;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.*;

public class MessageRepository {
    private final EntityManager em = JPAUtil.getEntityManager();

    private MessageEntity save(MessageEntity m) throws DatabaseException {
        while (em.find(MessageEntity.class, m.id()) != null) {
            m.setRandomID();
        }

        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            MessageEntity message = em.merge(m);
            transaction.commit();
            return message;
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new DatabaseException(e);
        }
    }

    public MessageEntity create(ChatEntity chat, ChatMessageInputDTO input, TauMemberEntity member)
            throws DatabaseException, NotFoundException {
        MessageEntity message = new MessageEntity(chat, input, member);
        Set<MessageEntity> messageEntities = new HashSet<>();
        Set<UUID> replies = input.repliedToMessages();
        if (replies != null) {
            for (UUID r : replies) {
                messageEntities.add(findById(r).orElseThrow(NotFoundException::new));
            }
        }
        message.setRepliedToMessages(messageEntities);

        return save(message);
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
            String q = "FROM MessageEntity WHERE chat = :chat ORDER BY creationDate DESC";
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
