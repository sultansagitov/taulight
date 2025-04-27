package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

public class RolesDTO {
    @JsonProperty("all-roles")
    public Set<String> allRoles;
    @JsonProperty("member-roles")
    public Set<String> memberRoles;

    @SuppressWarnings("unused")
    public RolesDTO() {}

    public RolesDTO(Set<String> allRoles, Set<String> memberRoles) {
        this.allRoles = allRoles;
        this.memberRoles = memberRoles;
    }
}
