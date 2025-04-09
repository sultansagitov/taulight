package net.result.taulight.db;

import net.result.sandnode.db.MemberEntity;

import javax.persistence.*;
import java.util.Collection;
import java.util.HashSet;

@Entity
public class ChannelEntity extends ChatEntity {
    private String title;

    @ManyToOne(cascade = CascadeType.ALL)
    private MemberEntity owner;

    @ManyToMany(cascade = CascadeType.ALL)
    private Collection<MemberEntity> members = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL)
    private Collection<InviteCodeEntity> inviteCodes = new HashSet<>();

    public ChannelEntity() {
        super();
    }

    public ChannelEntity(String title, MemberEntity owner) {
        super();
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

    public Collection<MemberEntity> members() {
        return members;
    }

    public void setMembers(Collection<MemberEntity> members) {
        this.members = members;
    }

    public Collection<InviteCodeEntity> inviteCodes() {
        return inviteCodes;
    }

    public void setInviteCodes(Collection<InviteCodeEntity> inviteCodes) {
        this.inviteCodes = inviteCodes;
    }

}
