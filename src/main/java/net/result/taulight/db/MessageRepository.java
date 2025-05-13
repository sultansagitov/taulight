package net.result.taulight.db;

import net.result.sandnode.util.JPAUtil;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.error.NotFoundException;
import net.result.sandnode.util.Container;
import net.result.taulight.dto.ChatMessageInputDTO;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.*;

public class MessageRepository {
    private final EntityManager em;

    public MessageRepository(Container container) {
        em = container.get(JPAUtil.class).getEntityManager();
    }

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
        MessageEntity managed = save(new MessageEntity(chat, input, member));
        Set<MessageEntity> messageEntities = new HashSet<>();
        Set<UUID> replies = input.repliedToMessages;
        if (replies != null) {
            for (UUID r : replies) {
                messageEntities.add(findById(r).orElseThrow(NotFoundException::new));
            }
        }
        managed.setRepliedToMessages(messageEntities);

        chat.messages().add(managed);
        em.merge(chat);

        return managed;
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
            String q = "FROM MessageEntity WHERE chat = :chat ORDER BY creationDate DESC, id DESC";
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

    public Collection<MessageEntity> findLastMessagesByChats(Set<UUID> accessibleChatIds) throws DatabaseException {
        if (accessibleChatIds == null || accessibleChatIds.isEmpty()) return Collections.emptyList();

        try {
            String q = """
                SELECT m FROM MessageEntity m
                WHERE m.id IN (
                    SELECT sub.id FROM MessageEntity sub
                    WHERE sub.chat.id IN :chatIds
                    AND sub.creationDate = (
                        SELECT MAX(innerM.creationDate)
                        FROM MessageEntity innerM
                        WHERE innerM.chat.id = sub.chat.id
                    )
                    ORDER BY sub.chat.id
                )
            """;

            return em.createQuery(q, MessageEntity.class)
                    .setParameter("chatIds", accessibleChatIds)
                    .getResultList();

        } catch (Exception e) {
            throw new DatabaseException("Failed to fetch last messages by chats", e);
        }
    }
}
