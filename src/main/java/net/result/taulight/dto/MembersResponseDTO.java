package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Collection;

@NoArgsConstructor
@AllArgsConstructor
public class MembersResponseDTO {
    @JsonProperty
    public Collection<ChatMemberDTO> members;
    @JsonProperty
    public Collection<RoleDTO> roles;
}
