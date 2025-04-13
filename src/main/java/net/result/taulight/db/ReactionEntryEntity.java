package net.result.taulight.db;

import net.result.sandnode.db.SandnodeEntity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@SuppressWarnings("unused")
@Entity
public class ReactionEntryEntity extends SandnodeEntity {
    @ManyToOne(cascade = CascadeType.ALL)
    private TauMemberEntity member;

    @ManyToOne(cascade = CascadeType.ALL)
    private MessageEntity message;

    @ManyToOne(cascade = CascadeType.ALL)
    private ReactionTypeEntity reactionType;

    public ReactionEntryEntity() {}

    public ReactionEntryEntity(TauMemberEntity member, MessageEntity message, ReactionTypeEntity reactionType) {
        this.message = message;
        this.reactionType = reactionType;
        this.member = member;
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
