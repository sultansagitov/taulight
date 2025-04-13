package net.result.taulight.db;

import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.db.SandnodeEntity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@SuppressWarnings("unused")
@Entity
public class ReactionEntryEntity extends SandnodeEntity {
    @ManyToOne(cascade = CascadeType.ALL)
    private MemberEntity member;

    @ManyToOne(cascade = CascadeType.ALL)
    private MessageEntity message;

    @ManyToOne(cascade = CascadeType.ALL)
    private ReactionTypeEntity reactionType;

    public ReactionEntryEntity() {
        super();
    }

    public ReactionEntryEntity(MemberEntity member, MessageEntity message, ReactionTypeEntity reactionType) {
        super();
        this.message = message;
        this.reactionType = reactionType;
        this.member = member;
    }

    public MemberEntity member() {
        return member;
    }

    public void setMember(MemberEntity member) {
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
