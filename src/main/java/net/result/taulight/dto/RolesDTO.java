package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

/**
 * Data Transfer Object representing a set of all available roles and the roles of a specific member.
 */
public class RolesDTO {
    /** Set of all available roles in the system. */
    @JsonProperty("all-roles")
    public Set<String> allRoles;
    /** Set of roles assigned to the member. */
    @JsonProperty("member-roles")
    public Set<String> memberRoles;

    /** Default constructor. */
    @SuppressWarnings("unused")
    public RolesDTO() {}

    /**
     * Constructs a RolesDTO.
     *
     * @param allRoles all available roles
     * @param memberRoles the member's roles
     */
    public RolesDTO(Set<String> allRoles, Set<String> memberRoles) {
        this.allRoles = allRoles;
        this.memberRoles = memberRoles;
    }
}
