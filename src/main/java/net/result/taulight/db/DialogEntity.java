package net.result.taulight.db;

import net.result.sandnode.db.MemberEntity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class DialogEntity extends ChatEntity {
    @ManyToOne(cascade = CascadeType.ALL)
    private MemberEntity firstMember;

    @ManyToOne(cascade = CascadeType.ALL)
    private MemberEntity secondMember;

    public DialogEntity() {
        super();
    }

    public DialogEntity(MemberEntity firstMember, MemberEntity secondMember) {
        super();
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
        if (member.equals(firstMember)) return secondMember;
        if (member.equals(secondMember)) return firstMember;
        throw new IllegalArgumentException("Member not part of this dialog");
    }

    @Override
    public String toString() {
        return "<Dialog %s and %s>".formatted(firstMember.nickname(), secondMember.nickname());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DialogEntity other)) return false;
        if (!this.id().equals(other.id())) return false;

        if (firstMember.equals(other.firstMember) && secondMember.equals(other.secondMember)) return true;
        return firstMember.equals(other.secondMember) && secondMember.equals(other.firstMember);
    }
}
