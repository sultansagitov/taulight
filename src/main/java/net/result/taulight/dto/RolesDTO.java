package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.taulight.db.Permission;

import java.util.Set;

/**
 * Data Transfer Object representing a set of all available roles and the roles of a specific member.
 */
public class RolesDTO {
    /** Set of all available roles in the system. */
    @JsonProperty("all-roles")
    public Set<RoleDTO> allRoles;
    /** Set of roles assigned to the member. */
    @JsonProperty("member-roles")
    public Set<String> memberRoles;
    /** Set of default permissions . */
    @JsonProperty
    public Set<Permission> permissions;

    /** Default constructor. */
    @SuppressWarnings("unused")
    public RolesDTO() {}

    /**
     * Constructs a RolesDTO.
     *
     * @param allRoles     all available roles
     * @param memberRoles  roles assigned to the member
     * @param permissions  granted permissions
     */
    public RolesDTO(Set<RoleDTO> allRoles, Set<String> memberRoles, Set<Permission> permissions) {
        this.allRoles = allRoles;
        this.memberRoles = memberRoles;
        this.permissions = permissions;
    }

}
