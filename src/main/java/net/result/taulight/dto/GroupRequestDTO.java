package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class GroupRequestDTO {
    public enum DataType {CREATE, INVITE, LEAVE, CH_CODES, MY_CODES, SET_AVATAR, GET_AVATAR}

    @JsonProperty
    public DataType type;
    @JsonProperty
    public String title;
    @JsonProperty("chat-id")
    public UUID chatID;
    @JsonProperty("other-nickname")
    public String otherNickname;
    @JsonProperty("expiration-time")
    public String expirationTime;

    @SuppressWarnings("unused")
    public GroupRequestDTO() {}

    public GroupRequestDTO(DataType type) {
        this.type = type;
    }
}
