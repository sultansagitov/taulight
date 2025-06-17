package net.result.taulight.db;

import jakarta.persistence.*;
import net.result.sandnode.db.BaseEntity;
import net.result.sandnode.db.MemberEntity;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
@Entity
public class TauMemberEntity extends BaseEntity {
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

    public TauMemberEntity() {}

    public TauMemberEntity(MemberEntity member) {
        setMember(member);
        setShowStatus(false);
    }

    public boolean isShowStatus() {
        return showStatus;
    }

    public void setShowStatus(boolean showStatus) {
        this.showStatus = showStatus;
    }

    public MemberEntity member() {
        return member;
    }

    public void setMember(MemberEntity member) {
        this.member = member;
    }

    public Set<ReactionPackageEntity> reactionPackages() {
        return reactionPackages;
    }

    public void setReactionPackages(Set<ReactionPackageEntity> reactionPackages) {
        this.reactionPackages = reactionPackages;
    }

    public Set<ReactionEntryEntity> reactionEntries() {
        return reactionEntries;
    }

    public void setReactionEntries(Set<ReactionEntryEntity> reactionEntries) {
        this.reactionEntries = reactionEntries;
    }

    public Set<GroupEntity> groups() {
        return groups;
    }

    public void setGroups(Set<GroupEntity> groups) {
        this.groups = groups;
    }

    public Set<DialogEntity> dialogs() {
        var all = new HashSet<>(dialogsAsFirst());
        all.addAll(dialogsAsSecond());
        return all;
    }

    public Set<DialogEntity> dialogsAsFirst() {
        return dialogsAsFirst;
    }

    public void setDialogsAsFirst(Set<DialogEntity> dialogsAsFirst) {
        this.dialogsAsFirst = dialogsAsFirst;
    }

    public Set<DialogEntity> dialogsAsSecond() {
        return dialogsAsSecond;
    }

    public void setDialogsAsSecond(Set<DialogEntity> dialogsAsSecond) {
        this.dialogsAsSecond = dialogsAsSecond;
    }

    public Set<InviteCodeEntity> inviteCodesAsReceiver() {
        return inviteCodesAsReceiver;
    }

    public void setInviteCodesAsReceiver(Set<InviteCodeEntity> inviteCodesAsReceiver) {
        this.inviteCodesAsReceiver = inviteCodesAsReceiver;
    }

    public Set<InviteCodeEntity> inviteCodesAsSender() {
        return inviteCodesAsSender;
    }

    public void setInviteCodesAsSender(Set<InviteCodeEntity> inviteCodesAsSender) {
        this.inviteCodesAsSender = inviteCodesAsSender;
    }

    public Set<RoleEntity> roles() {
        return roles;
    }

    public void setRoles(Set<RoleEntity> roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return "<MemberEntity %s %s>".formatted(id(), member().nickname());
    }
}
