package net.result.taulight.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import net.result.sandnode.entity.BaseEntity;
import net.result.taulight.dto.ReactionDTO;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@Entity
public class ReactionEntryEntity extends BaseEntity {
    @ManyToOne
    private TauMemberEntity member;

    @ManyToOne
    private MessageEntity message;

    @ManyToOne
    private ReactionTypeEntity reactionType;

    public ReactionEntryEntity() {}

    public ReactionEntryEntity(TauMemberEntity member, MessageEntity message, ReactionTypeEntity reactionType) {
        this.message = message;
        this.reactionType = reactionType;
        this.member = member;
    }

    public @NotNull ReactionDTO toDTO(boolean isReact) {
        return new ReactionDTO(
                isReact,
                member().getMember().getNickname(),
                message().chat().id(),
                message().id(),
                reactionType().reactionPackage().name(),
                reactionType().name()
        );
    }

    public TauMemberEntity member() {
        return member;
    }

    public void setMember(TauMemberEntity member) {
        this.member = member;
    }

    public MessageEntity message() {
        return message;
    }

    public void setMessage(MessageEntity message) {
        this.message = message;
    }

    public ReactionTypeEntity reactionType() {
        return reactionType;
    }

    public void setReactionType(ReactionTypeEntity reactionType) {
        this.reactionType = reactionType;
    }
}
