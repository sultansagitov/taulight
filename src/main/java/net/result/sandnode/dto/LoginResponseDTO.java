package net.result.sandnode.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class LoginResponseDTO {
    @JsonProperty
    public String nickname;

    @JsonProperty("key-id")
    public UUID keyID;

    @SuppressWarnings("unused")
    public LoginResponseDTO() {
    }

    public LoginResponseDTO(String nickname, UUID keyID) {
        this.nickname = nickname;
        this.keyID = keyID;
    }
}
