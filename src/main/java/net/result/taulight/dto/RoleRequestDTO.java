package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;


public record RoleRequestDTO(
        @JsonProperty("type") DataType dataType,
        @JsonProperty("chat-id") UUID chatID,
        @JsonProperty("role-id") UUID roleID,
        @JsonProperty("role-name") String roleName,
        @JsonProperty("nickname") String nickname
) {
    public enum DataType {GET, CREATE, ADD}
}
