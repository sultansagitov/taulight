package net.result.taulight.db;

import net.result.sandnode.db.SandnodeEntity;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("unused")
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class ChatEntity extends SandnodeEntity {
    @OneToMany(cascade = CascadeType.ALL)
    private Set<MessageEntity> messages = new HashSet<>();

    public ChatEntity() {
        super();
    }

    public ChatEntity(UUID id, ZonedDateTime creationDate) {
        super(id, creationDate);
    }

    public Set<MessageEntity> messages() {
        return messages;
    }

    public void setMessages(Set<MessageEntity> messages) {
        this.messages = messages;
    }

}
