package net.result.sandnode.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginResponseDTO {
    @JsonProperty
    public String nickname;

    @SuppressWarnings("unused")
    public LoginResponseDTO() {}

    public LoginResponseDTO(String nickname) {
        this.nickname = nickname;
    }
}
