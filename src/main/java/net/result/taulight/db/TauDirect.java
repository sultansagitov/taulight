package net.result.taulight.db;

import net.result.sandnode.db.Member;

import java.util.UUID;

public class TauDirect extends TauChat {
    private final Member member1;
    private final Member member2;

    public TauDirect(UUID id, Member member1, Member member2) {
        super(id);
        this.member1 = member1;
        this.member2 = member2;
    }

    public TauDirect(Member member1, Member member2) {
        this(UUID.randomUUID(), member1, member2);
    }

    public Member firstMember() {
        return member1;
    }

    public Member secondMember() {
        return member2;
    }

    public Member otherMember(Member member) {
        if (member.equals(member1)) return member2;
        if (member.equals(member2)) return member1;
        throw new IllegalArgumentException("Member not part of this direct chat");
    }

    @Override
    public String toString() {
        return "<Direct Chat: %s and %s>".formatted(member1.id(), member2.id());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TauDirect other)) return false;
        if (!this.id().equals(other.id())) return false;

        if (member1.equals(other.member1) && member2.equals(other.member2)) return true;
        return member1.equals(other.member2) && member2.equals(other.member1);
    }

    @Override
    public int hashCode() {
        return id().hashCode();
    }
}