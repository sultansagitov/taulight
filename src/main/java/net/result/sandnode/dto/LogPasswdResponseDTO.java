package net.result.sandnode.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class LogPasswdResponseDTO {
    @JsonProperty
    public String token;

    @JsonProperty
    public UUID keyID;

    @SuppressWarnings("unused")
    public LogPasswdResponseDTO() {}

    public LogPasswdResponseDTO(String token, UUID keyID) {
        this.token = token;
        this.keyID = keyID;
    }
}
