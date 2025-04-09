package net.result.taulight.db;

import net.result.sandnode.db.MemberEntity;
import net.result.taulight.dto.ChatInfoDTO;
import net.result.taulight.dto.ChatInfoPropDTO;

import javax.persistence.Entity;
import java.util.Collection;
import java.util.Collections;

@Entity
public class DialogEntity extends ChatEntity {
    private MemberEntity firstMember;
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
    public boolean hasMatchingProps(Collection<ChatInfoPropDTO> chatInfoProps) {
        return !Collections.disjoint(chatInfoProps, ChatInfoPropDTO.dialogAll());
    }

    @Override
    public ChatInfoDTO getInfo(MemberEntity member, Collection<ChatInfoPropDTO> chatInfoProps) {
        return ChatInfoDTO.dialog(this, member, chatInfoProps);
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
