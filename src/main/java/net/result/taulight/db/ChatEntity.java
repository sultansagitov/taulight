package net.result.taulight.db;

import net.result.sandnode.db.SandnodeEntity;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class ChatEntity extends SandnodeEntity {
    @OneToMany(cascade = CascadeType.ALL)
    private Collection<MessageEntity> messages = new HashSet<>();

    public ChatEntity() {
        super();
    }

    public ChatEntity(UUID id, ZonedDateTime creationDate) {
        super(id, creationDate);
    }

    public Collection<MessageEntity> messages() {
        return messages;
    }

    public void setMessages(Collection<MessageEntity> messages) {
        this.messages = messages;
    }

}
