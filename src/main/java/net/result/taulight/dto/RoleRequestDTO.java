package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class RoleRequestDTO {
    public enum DataType {GET, CREATE, ADD}

    @JsonProperty("type")
    public DataType dataType = null;
    @JsonProperty("chat-id")
    public UUID chatID = null;
    @JsonProperty("role")
    public String roleName = null;

    @JsonProperty("nickname")
    public String nickname = null;

    @SuppressWarnings("unused")
    public RoleRequestDTO() {
    }

    public RoleRequestDTO(DataType dataType, UUID chatID, String roleName, String nickname) {
        this.dataType = dataType;
        this.chatID = chatID;
        this.roleName = roleName;
        this.nickname = nickname;
    }
}
