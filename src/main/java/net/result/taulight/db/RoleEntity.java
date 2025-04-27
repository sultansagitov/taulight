package net.result.taulight.db;

import net.result.sandnode.db.SandnodeEntity;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
@Entity
public class RoleEntity extends SandnodeEntity {
    private String name;

    @ManyToOne
    private ChannelEntity channel;

    @ManyToMany
    private Set<TauMemberEntity> members = new HashSet<>();

    public RoleEntity() {}

    public RoleEntity(ChannelEntity channel, String role) {
        setChannel(channel);
        setName(role);
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ChannelEntity channel() {
        return channel;
    }

    public void setChannel(ChannelEntity channel) {
        this.channel = channel;
    }

    public Set<TauMemberEntity> members() {
        return members;
    }

    public void setMembers(Set<TauMemberEntity> members) {
        this.members = members;
    }
}
