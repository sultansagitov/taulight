package net.result.taulight.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import net.result.sandnode.entity.FileEntity;
import net.result.taulight.dto.ChatInfoDTO;
import net.result.taulight.dto.ChatInfoPropDTO;
import net.result.taulight.dto.ChatMessageViewDTO;

import java.util.Collection;

@Entity
public class DialogEntity extends ChatEntity {
    @ManyToOne
    @JoinColumn(nullable = false)
    private TauMemberEntity firstMember;

    @ManyToOne
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
        if (member.equals(firstMember)) return secondMember;
        if (member.equals(secondMember)) return firstMember;
        throw new IllegalArgumentException("Member not part of this dialog");
    }

    /**
     * Constructs a ChatInfoDTO for a dialog.
     *
     * @param member      the member requesting the data
     * @param infoProps   a collection of properties to include in the response
     * @param lastMessage the last message in the dialog (nullable)
     *
     * @return a populated ChatInfoDTO for a dialog
     */
    public ChatInfoDTO toDTO(
            TauMemberEntity member,
            Collection<ChatInfoPropDTO> infoProps,
            ChatMessageViewDTO lastMessage
    ) {
        ChatInfoDTO info = new ChatInfoDTO();
        info.chatType = ChatInfoDTO.ChatType.DIALOG;

        if (infoProps.contains(ChatInfoPropDTO.dialogID)) info.id = id();
        if (infoProps.contains(ChatInfoPropDTO.createdAt)) info.creationDate = creationDate();
        if (infoProps.contains(ChatInfoPropDTO.dialogOther))
            info.otherNickname = otherMember(member).member().nickname();
        if (infoProps.contains(ChatInfoPropDTO.lastMessage)) info.lastMessage = lastMessage;
        if (infoProps.contains(ChatInfoPropDTO.hasAvatar)) {
            FileEntity avatar = otherMember(member).member().avatar();
            info.avatar = avatar != null ? avatar.id() : null;
        }

        return info;
    }

    @Override
    public String toString() {
        return "<Dialog %s and %s>".formatted(firstMember.member().nickname(), secondMember.member().nickname());
    }
}
