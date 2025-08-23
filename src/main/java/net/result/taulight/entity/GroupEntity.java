package net.result.taulight.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.result.sandnode.entity.FileEntity;
import net.result.taulight.db.Permission;
import net.result.taulight.dto.ChatInfoDTO;
import net.result.taulight.dto.ChatInfoPropDTO;
import net.result.taulight.dto.ChatMessageViewDTO;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@Entity
public class GroupEntity extends ChatEntity {
    private String title;

    @OneToOne
    private FileEntity avatar;

    @ManyToOne
    private TauMemberEntity owner;

    @ManyToMany
    @JoinTable(
            name = "group_members",
            joinColumns = @JoinColumn(name = "group_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "member_id", nullable = false)
    )
    private Set<TauMemberEntity> members = new HashSet<>();

    @OneToMany(mappedBy = "group", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private Set<InviteCodeEntity> inviteCodes = new HashSet<>();

    @OneToMany(mappedBy = "group", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private Set<RoleEntity> roles = new HashSet<>();

    @ElementCollection(targetClass = Permission.class)
    @Enumerated(EnumType.STRING)
    private Set<Permission> permissions = new HashSet<>();

    public GroupEntity(String title, TauMemberEntity owner) {
        this.title = title;
        this.owner = owner;
    }

    /**
     * Creates a ChatInfoDTO for a group.
     *
     * @param member      the member requesting the data
     * @param infoProps   a collection of properties to include in the response
     * @param lastMessage the last message in the group (nullable)
     * @return a populated ChatInfoDTO for a group
     */
    public ChatInfoDTO toDTO(
            TauMemberEntity member,
            Collection<ChatInfoPropDTO> infoProps,
            ChatMessageViewDTO lastMessage
    ) {
        ChatInfoDTO info = new ChatInfoDTO();
        info.chatType = ChatInfoDTO.ChatType.GROUP;

        if (infoProps.contains(ChatInfoPropDTO.groupID)) info.id = id();
        if (infoProps.contains(ChatInfoPropDTO.createdAt)) info.creationDate = getCreationDate();
        if (infoProps.contains(ChatInfoPropDTO.groupTitle)) info.title = getTitle();
        if (infoProps.contains(ChatInfoPropDTO.groupOwner)) info.ownerID = getOwner().getMember().getNickname();
        if (infoProps.contains(ChatInfoPropDTO.groupIsMy)) info.groupIsMy = getOwner() == member;
        if (infoProps.contains(ChatInfoPropDTO.lastMessage)) info.lastMessage = lastMessage;
        if (infoProps.contains(ChatInfoPropDTO.hasAvatar))
            info.avatar = getAvatar() != null ? getAvatar().id() : null;

        return info;
    }

    @Override
    public String toString() {
        return "<GroupEntity %s>".formatted(id());
    }

}
