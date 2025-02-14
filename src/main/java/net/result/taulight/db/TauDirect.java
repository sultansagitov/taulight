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

    public Member getMember1() {
        return member1;
    }

    public Member getMember2() {
        return member2;
    }

    public Member getOtherMember(Member member) {
        if (member.equals(member1)) {
            return member2;
        } else if (member.equals(member2)) {
            return member1;
        }
        throw new IllegalArgumentException("Member not part of this direct chat");
    }

    @Override
    public String toString() {
        return String.format("<Direct Chat: %s and %s>", 
            member1.getID(), 
            member2.getID()
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TauDirect other)) return false;
        
        return this.getID().equals(other.getID()) &&
               ((member1.equals(other.member1) && member2.equals(other.member2)) ||
                (member1.equals(other.member2) && member2.equals(other.member1)));
    }

    @Override
    public int hashCode() {
        return getID().hashCode();
    }
}