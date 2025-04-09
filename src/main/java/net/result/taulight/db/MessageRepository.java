package net.result.taulight.db;

import net.result.sandnode.exception.DatabaseException;
import net.result.taulight.dto.ChatMessageViewDTO;
import net.result.taulight.exception.AlreadyExistingRecordException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MessageRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public MessageEntity save(MessageEntity message) throws AlreadyExistingRecordException {
        TypedQuery<MessageEntity> query = entityManager.createQuery(
                "SELECT m FROM MessageEntity m WHERE m.id = :id", MessageEntity.class);
        query.setParameter("id", message.id());

        if (!query.getResultList().isEmpty()) {
            throw new AlreadyExistingRecordException("Message", "id", message.id());
        }

        entityManager.persist(message);
        return message;
    }

    public Optional<MessageEntity> findById(UUID id) throws DatabaseException {
        try {
            return Optional.ofNullable(entityManager.find(MessageEntity.class, id));
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public void remove(MessageEntity message) throws DatabaseException {
        entityManager.remove(message);
    }

    public List<ChatMessageViewDTO> findMessagesByChat(ChatEntity chat, int index, int size) throws DatabaseException {
        return null;
    }

    public long countMessagesByChat(ChatEntity chat) throws DatabaseException {
        return 0;
    }
}