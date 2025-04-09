package net.result.taulight.db;

import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.db.SandnodeEntity;

import javax.persistence.ManyToOne;

public class ReactionEntry extends SandnodeEntity {
    @ManyToOne
    private MemberEntity member;
    @ManyToOne
    private MessageEntity message;
    @ManyToOne
    private ReactionTypeEntity reactionType;

    @SuppressWarnings("unused")
    public ReactionEntry() {
        super();
    }

    public ReactionEntry(MemberEntity member, MessageEntity message, ReactionTypeEntity reactionType) {
        super();
        this.message = message;
        this.reactionType = reactionType;
        this.member = member;
    }

    public MemberEntity member() { return member; }

    public MessageEntity message() { return message; }

    public ReactionTypeEntity reactionType() { return reactionType; }
}
