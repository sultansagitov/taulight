package net.result.taulight.db;

import jakarta.persistence.*;
import net.result.sandnode.db.FileEntity;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
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

    public GroupEntity() {}

    public GroupEntity(String title, TauMemberEntity owner) {
        this.title = title;
        this.owner = owner;
    }

    public String title() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public FileEntity avatar() {
        return avatar;
    }

    public void setAvatar(FileEntity avatar) {
        this.avatar = avatar;
    }

    public TauMemberEntity owner() {
        return owner;
    }

    public void setOwner(TauMemberEntity owner) {
        this.owner = owner;
    }

    public Set<TauMemberEntity> members() {
        return members;
    }

    public void setMembers(Set<TauMemberEntity> members) {
        this.members = members;
    }

    public Set<InviteCodeEntity> inviteCodes() {
        return inviteCodes;
    }

    public void setInviteCodes(Set<InviteCodeEntity> inviteCodes) {
        this.inviteCodes = inviteCodes;
    }

    @Override
    public String toString() {
        return "<GroupEntity %s>".formatted(id());
    }

    public Set<RoleEntity> roles() {
        return roles;
    }

    public void setRoles(Set<RoleEntity> roles) {
        this.roles = roles;
    }
}
