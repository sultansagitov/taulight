package net.result.taulight.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.result.sandnode.entity.BaseEntity;
import net.result.taulight.db.Permission;
import net.result.taulight.dto.RoleDTO;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
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

    public RoleEntity(GroupEntity group, String role) {
        setGroup(group);
        setName(role);
    }

    public @NotNull RoleDTO toDTO() {
        return new RoleDTO(id(), getName(), getPermissions());
    }

    @Override
    public String toString() {
        return "<RoleEntity %s %s permissions=%s members=%s>".formatted(
                id(),
                getName(),
                getPermissions(),
                getMembers()
        );
    }
}
