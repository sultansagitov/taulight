package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
public class GroupRequestDTO {
    public enum DataType {CREATE, INVITE, LEAVE, SET_AVATAR, GET_AVATAR}

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

    public GroupRequestDTO(DataType type) {
        this.type = type;
    }
}
