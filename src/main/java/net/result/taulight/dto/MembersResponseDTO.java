package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class MembersResponseDTO {
    @JsonProperty
    public Collection<ChatMemberDTO> members;
    @JsonProperty
    public Collection<RoleDTO> roles;

    @SuppressWarnings("unused")
    public MembersResponseDTO() {}

    public MembersResponseDTO(@NotNull Collection<ChatMemberDTO> members, @Nullable Collection<RoleDTO> roles) {
        this.members = members;
        this.roles = roles;
    }
}
