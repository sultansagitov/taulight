package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.taulight.db.RoleEntity;

import java.util.UUID;

public class RoleDTO {
    @JsonProperty
    public UUID id;
    @JsonProperty
    public String name;

    @SuppressWarnings("unused")
    public RoleDTO() {}

    public RoleDTO(RoleEntity entity) {
        id = entity.id();
        name = entity.name();
    }
}
