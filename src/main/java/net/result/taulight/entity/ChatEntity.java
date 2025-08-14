package net.result.taulight.entity;

import jakarta.persistence.*;
import net.result.sandnode.entity.BaseEntity;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.util.SysMessages;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class ChatEntity extends BaseEntity {
    @OneToMany(mappedBy = "chat", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private Set<MessageEntity> messages = new HashSet<>();

    public ChatEntity() {}

    public Set<MessageEntity> messages() {
        return messages;
    }

    public void setMessages(Set<MessageEntity> messages) {
        this.messages = messages;
    }

    public ChatMessageInputDTO toInput(@NotNull TauMemberEntity member, SysMessages sysMessages) {
        return new ChatMessageInputDTO()
                .setSys(true)
                .setChatID(id())
                .setNickname(member.member().nickname())
                .setContent(sysMessages.toString())
                .setSentDatetimeNow();
    }
}
