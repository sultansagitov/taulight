package net.result.taulight.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import net.result.sandnode.entity.EncryptedKeyEntity;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.error.NotFoundException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.util.Container;
import net.result.sandnode.db.JPAUtil;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.entity.ChatEntity;
import net.result.taulight.entity.MessageEntity;
import net.result.taulight.entity.TauMemberEntity;

import java.util.*;
import java.util.stream.Collectors;

public class MessageRepository {
    private final JPAUtil jpaUtil;
    private final MessageFileRepository messageFileRepo;

    public MessageRepository(Container container) {
        jpaUtil = container.get(JPAUtil.class);
        messageFileRepo = container.get(MessageFileRepository.class);
    }

    public MessageEntity create(ChatEntity chat, ChatMessageInputDTO input, TauMemberEntity member) {
        return create(chat, input, member, null);
    }

    public MessageEntity create(
            ChatEntity chat,
            ChatMessageInputDTO input,
            TauMemberEntity member,
            EncryptedKeyEntity key
    ) {
        EntityManager em = jpaUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            MessageEntity managed = em.merge(new MessageEntity(chat, input, member, key));
            Set<UUID> replies = input.repliedToMessages;
            if (replies != null) {
                Set<MessageEntity> messageEntities = replies.stream()
                        .map(r -> jpaUtil.find(MessageEntity.class, r).orElseThrow(NotFoundException::new))
                        .collect(Collectors.toSet());
                managed.setRepliedToMessages(messageEntities);
            }

            chat.getMessages().add(managed);
            em.merge(chat);
            transaction.commit();

            if (input.fileIDs != null && !input.fileIDs.isEmpty()) {
                messageFileRepo.setMessage(managed, input.fileIDs);
            }

            return managed;
        } catch (UnauthorizedException e) {
            transaction.rollback();
            throw e;
        } catch (Exception e) {
            transaction.rollback();
            throw new DatabaseException(e);
        }
    }

    public List<MessageEntity> findMessagesByChat(ChatEntity chat, int index, int size) {
        EntityManager em = jpaUtil.getEntityManager();
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

    public long countMessagesByChat(ChatEntity chat) {
        EntityManager em = jpaUtil.getEntityManager();
        try {
            String q = "SELECT COUNT(m) FROM MessageEntity m WHERE m.chat = :chat";
            return em.createQuery(q, Long.class).setParameter("chat", chat).getSingleResult();
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public Collection<MessageEntity> findLastMessagesByChats(Set<UUID> accessibleChatIds) {
        EntityManager em = jpaUtil.getEntityManager();
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
            throw new DatabaseException(e);
        }
    }
}
