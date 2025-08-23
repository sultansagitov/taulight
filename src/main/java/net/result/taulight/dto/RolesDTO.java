package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.result.taulight.db.Permission;

import java.util.Set;
import java.util.UUID;

/**
 * Data Transfer Object representing a set of all available roles and the roles of a specific member.
 */
@NoArgsConstructor
@AllArgsConstructor
public class RolesDTO {
    /** Set of all available roles in the system. */
    @JsonProperty("all-roles")
    public Set<RoleDTO> allRoles;
    /** Set of roles assigned to the member. */
    @JsonProperty("member-roles")
    public Set<UUID> memberRoles;
    /** Set of default permissions . */
    @JsonProperty
    public Set<Permission> permissions;
}
