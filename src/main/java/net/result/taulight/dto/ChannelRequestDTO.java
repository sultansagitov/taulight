package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class ChannelRequestDTO {
    public enum DataType {CREATE, INVITE, LEAVE, CH_CODES, MY_CODES, SET_AVATAR, GET_AVATAR}

    @JsonProperty
    public DataType type;
    @JsonProperty
    public String title;
    @JsonProperty
    public UUID chatID;
    @JsonProperty
    public String otherNickname;
    @JsonProperty
    public String expirationTime;

    @SuppressWarnings("unused")
    public ChannelRequestDTO() {}

    public ChannelRequestDTO(DataType type) {
        this.type = type;
    }
}
