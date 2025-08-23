package net.result.taulight.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.result.sandnode.entity.BaseEntity;
import net.result.taulight.dto.ReactionDTO;
import org.jetbrains.annotations.NotNull;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ReactionEntryEntity extends BaseEntity {
    @ManyToOne
    private TauMemberEntity member;

    @ManyToOne
    private MessageEntity message;

    @ManyToOne
    private ReactionTypeEntity reactionType;

    public @NotNull ReactionDTO toDTO(boolean isReact) {
        return new ReactionDTO(
                isReact,
                getMember().getMember().getNickname(),
                getMessage().getChat().id(),
                getMessage().id(),
                getReactionType().getReactionPackage().getName(),
                getReactionType().getName()
        );
    }
}
