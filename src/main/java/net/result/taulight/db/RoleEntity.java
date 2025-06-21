package net.result.taulight.db;

import jakarta.persistence.*;
import net.result.sandnode.db.BaseEntity;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
@Entity
public class RoleEntity extends BaseEntity {
    private String name;

    @ManyToOne
    private GroupEntity group;

    @ManyToMany
    private Set<TauMemberEntity> members = new HashSet<>();

    @ElementCollection(targetClass = Permission.class)
    @Enumerated(EnumType.STRING)
    private Set<Permission> permissions = new HashSet<>();

    public RoleEntity() {}

    public RoleEntity(GroupEntity group, String role) {
        setGroup(group);
        setName(role);
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GroupEntity group() {
        return group;
    }

    public void setGroup(GroupEntity group) {
        this.group = group;
    }

    public Set<TauMemberEntity> members() {
        return members;
    }

    public void setMembers(Set<TauMemberEntity> members) {
        this.members = members;
    }

    public Set<Permission> permissions() {
        return permissions;
    }

    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }
}
