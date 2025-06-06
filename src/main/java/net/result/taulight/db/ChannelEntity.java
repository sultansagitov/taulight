package net.result.taulight.db;

import javax.persistence.*;
import java.util.Set;
import java.util.HashSet;

@SuppressWarnings("unused")
@Entity
public class ChannelEntity extends ChatEntity {
    private String title;
    private String contentType;
    private String filename;

    @ManyToOne
    private TauMemberEntity owner;

    @ManyToMany
    @JoinTable(
            name = "channel_members",
            joinColumns = @JoinColumn(name = "channel_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "member_id", nullable = false)
    )
    private Set<TauMemberEntity> members = new HashSet<>();

    @OneToMany(mappedBy = "channel", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private Set<InviteCodeEntity> inviteCodes = new HashSet<>();

    @OneToMany(mappedBy = "channel", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private Set<RoleEntity> roles = new HashSet<>();

    public ChannelEntity() {}

    public ChannelEntity(String title, TauMemberEntity owner) {
        this.title = title;
        this.owner = owner;
    }

    public String title() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String contentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String filename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
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
        return "<ChannelEntity %s>".formatted(id());
    }

    public Set<RoleEntity> roles() {
        return roles;
    }

    public void setRoles(Set<RoleEntity> roles) {
        this.roles = roles;
    }
}
