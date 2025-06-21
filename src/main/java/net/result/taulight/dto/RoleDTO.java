package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.taulight.db.Permission;
import net.result.taulight.db.RoleEntity;

import java.util.Collection;
import java.util.UUID;

public class RoleDTO {
    @JsonProperty
    public UUID id;
    @JsonProperty
    public String name;
    @JsonProperty
    public Collection<Permission> permissions;

    @SuppressWarnings("unused")
    public RoleDTO() {}

    public RoleDTO(RoleEntity entity) {
        id = entity.id();
        name = entity.name();
        permissions = entity.permissions();
    }
}
