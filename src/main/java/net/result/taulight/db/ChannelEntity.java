package net.result.taulight.db;

import net.result.sandnode.db.MemberEntity;

import javax.persistence.*;
import java.util.Set;
import java.util.HashSet;

@SuppressWarnings("unused")
@Entity
public class ChannelEntity extends ChatEntity {
    private String title;

    @ManyToOne(cascade = CascadeType.ALL)
    private MemberEntity owner;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "channel_members",
            joinColumns = @JoinColumn(name = "channel_id"),
            inverseJoinColumns = @JoinColumn(name = "member_id")
    )
    private Set<MemberEntity> members = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL)
    private Set<InviteCodeEntity> inviteCodes = new HashSet<>();

    public ChannelEntity() {}

    public ChannelEntity(String title, MemberEntity owner) {
        this.title = title;
        this.owner = owner;
    }

    public String title() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public MemberEntity owner() {
        return owner;
    }

    public void setOwner(MemberEntity owner) {
        this.owner = owner;
    }

    public Set<MemberEntity> members() {
        return members;
    }

    public void setMembers(Set<MemberEntity> members) {
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
