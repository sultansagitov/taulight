package net.result.taulight.db;

import javax.persistence.*;
import java.util.Set;
import java.util.HashSet;

@SuppressWarnings("unused")
@Entity
public class ChannelEntity extends ChatEntity {
    private String title;

    @ManyToOne
    private TauMemberEntity owner;

    @ManyToMany
    @JoinTable(
            name = "channel_members",
            joinColumns = @JoinColumn(name = "channel_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "member_id", nullable = false)
    )
    private Set<TauMemberEntity> members = new HashSet<>();

    @OneToMany(mappedBy = "channel", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<InviteCodeEntity> inviteCodes = new HashSet<>();

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
}
