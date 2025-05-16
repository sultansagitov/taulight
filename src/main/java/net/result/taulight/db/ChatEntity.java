package net.result.taulight.db;

import net.result.sandnode.db.SandnodeEntity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class ChatEntity extends SandnodeEntity {
    @OneToMany(mappedBy = "chat", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private Set<MessageEntity> messages = new HashSet<>();

    public ChatEntity() {}

    public Set<MessageEntity> messages() {
        return messages;
    }

    public void setMessages(Set<MessageEntity> messages) {
        this.messages = messages;
    }

}
