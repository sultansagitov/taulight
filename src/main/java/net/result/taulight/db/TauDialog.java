package net.result.taulight.db;

import net.result.sandnode.db.Member;
import net.result.taulight.message.ChatInfo;
import net.result.taulight.message.ChatInfoProp;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class TauDialog extends TauChat {
    private final Member member1;
    private final Member member2;

    public TauDialog(UUID id, ZonedDateTime creationDate, TauDatabase database, Member member1, Member member2) {
        super(id, creationDate, database);
        this.member1 = member1;
        this.member2 = member2;
    }

    public TauDialog(TauDatabase database, Member member1, Member member2) {
        super(database);
        this.member1 = member1;
        this.member2 = member2;
    }

    @Override
    public Collection<Member> getMembers() {
        return List.of(firstMember(), secondMember());
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
        throw new IllegalArgumentException("Member not part of this dialog");
    }

    @Override
    public boolean hasMatchingProps(Collection<ChatInfoProp> chatInfoProps) {
        return !Collections.disjoint(chatInfoProps, ChatInfoProp.dialogAll());
    }

    @Override
    public ChatInfo getInfo(Member member, Collection<ChatInfoProp> chatInfoProps) {
        return ChatInfo.dialog(this, member, chatInfoProps);
    }

    @Override
    public String toString() {
        return "<Dialog: %s and %s>".formatted(member1.id(), member2.id());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TauDialog other)) return false;
        if (!this.id().equals(other.id())) return false;

        if (member1.equals(other.member1) && member2.equals(other.member2)) return true;
        return member1.equals(other.member2) && member2.equals(other.member1);
    }
}