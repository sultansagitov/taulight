package net.result.taulight.db;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class DialogEntity extends ChatEntity {
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    private TauMemberEntity firstMember;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    private TauMemberEntity secondMember;

    public DialogEntity() {}

    public DialogEntity(TauMemberEntity firstMember, TauMemberEntity secondMember) {
        this.firstMember = firstMember;
        this.secondMember = secondMember;
    }

    public TauMemberEntity firstMember() {
        return firstMember;
    }

    public TauMemberEntity secondMember() {
        return secondMember;
    }

    public TauMemberEntity otherMember(TauMemberEntity member) {
        if (member == firstMember) return secondMember;
        if (member == secondMember) return firstMember;
        throw new IllegalArgumentException("Member not part of this dialog");
    }

    @Override
    public String toString() {
        return "<Dialog %s and %s>".formatted(firstMember.member().nickname(), secondMember.member().nickname());
    }
}
