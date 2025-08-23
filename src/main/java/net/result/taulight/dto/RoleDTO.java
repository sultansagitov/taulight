package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.result.taulight.db.Permission;

import java.util.Collection;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {
    @JsonProperty
    public UUID id;
    @JsonProperty
    public String name;
    @JsonProperty
    public Collection<Permission> permissions;
}
