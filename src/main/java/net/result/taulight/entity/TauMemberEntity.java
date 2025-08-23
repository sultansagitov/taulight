package net.result.taulight.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.result.sandnode.entity.BaseEntity;
import net.result.sandnode.entity.MemberEntity;
import net.result.taulight.dto.ChatMemberDTO;
import net.result.taulight.dto.MemberStatus;
import net.result.taulight.dto.TauMemberSettingsDTO;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class TauMemberEntity extends BaseEntity {
    @Column()
    private boolean showStatus;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private MemberEntity member;

    @OneToMany(mappedBy = "author", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<ReactionPackageEntity> reactionPackages = new HashSet<>();

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<ReactionEntryEntity> reactionEntries = new HashSet<>();

    @ManyToMany(mappedBy = "members", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<GroupEntity> groups = new HashSet<>();

    @OneToMany(mappedBy = "firstMember", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<DialogEntity> dialogsAsFirst = new HashSet<>();

    @OneToMany(mappedBy = "secondMember", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<DialogEntity> dialogsAsSecond = new HashSet<>();

    @OneToMany(mappedBy = "receiver", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private Set<InviteCodeEntity> inviteCodesAsReceiver = new HashSet<>();

    @OneToMany(mappedBy = "sender", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private Set<InviteCodeEntity> inviteCodesAsSender = new HashSet<>();

    @ManyToMany(mappedBy = "members", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<RoleEntity> roles = new HashSet<>();

    public TauMemberEntity(MemberEntity member) {
        setMember(member);
        setShowStatus(false);
    }

    public Set<DialogEntity> getDialogs() {
        var all = new HashSet<>(getDialogsAsFirst());
        all.addAll(getDialogsAsSecond());
        return all;
    }

    public @NotNull ChatMemberDTO toChatMemberDTO(List<UUID> roleIds) {
        return new ChatMemberDTO(
                getMember().getNickname(),
                isShowStatus() ? MemberStatus.OFFLINE : MemberStatus.HIDDEN,
                roleIds
        );
    }

    public @NotNull TauMemberSettingsDTO toSettingsDTO() {
        return new TauMemberSettingsDTO(isShowStatus());
    }

    @Override
    public String toString() {
        return "<TauMemberEntity %s %s>".formatted(id(), getMember().getNickname());
    }
}
