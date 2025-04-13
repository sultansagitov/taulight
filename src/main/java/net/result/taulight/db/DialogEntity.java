package net.result.taulight.db;

import net.result.sandnode.db.MemberEntity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class DialogEntity extends ChatEntity {
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    private MemberEntity firstMember;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    private MemberEntity secondMember;

    public DialogEntity() {}

    public DialogEntity(MemberEntity firstMember, MemberEntity secondMember) {
        this.firstMember = firstMember;
        this.secondMember = secondMember;
    }

    public MemberEntity firstMember() {
        return firstMember;
    }

    public MemberEntity secondMember() {
        return secondMember;
    }

    public MemberEntity otherMember(MemberEntity member) {
        if (member == firstMember) return secondMember;
        if (member == secondMember) return firstMember;
        throw new IllegalArgumentException("Member not part of this dialog");
    }

    @Override
    public String toString() {
        return "<Dialog %s and %s>".formatted(firstMember.nickname(), secondMember.nickname());
    }
}
