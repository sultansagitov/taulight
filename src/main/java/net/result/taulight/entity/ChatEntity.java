package net.result.taulight.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.result.sandnode.entity.BaseEntity;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.util.SysMessages;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class ChatEntity extends BaseEntity {
    @OneToMany(mappedBy = "chat", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private Set<MessageEntity> messages = new HashSet<>();

    public ChatMessageInputDTO toInput(@NotNull TauMemberEntity member, SysMessages sysMessages) {
        return new ChatMessageInputDTO()
                .setSys(true)
                .setChatID(id())
                .setNickname(member.getMember().getNickname())
                .setContent(sysMessages.toString())
                .setSentDatetimeNow();
    }
}
