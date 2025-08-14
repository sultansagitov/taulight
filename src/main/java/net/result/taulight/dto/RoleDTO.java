package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.taulight.db.Permission;

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

    public RoleDTO(UUID id, String name, Collection<Permission> permissions) {
        this.id = id;
        this.name = name;
        this.permissions = permissions;
    }
}
