package net.result.sandnode.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class RegistrationResponseDTO {
    @JsonProperty
    public String token;
    @JsonProperty("key-id")
    public UUID keyID;

    @SuppressWarnings("unused")
    public RegistrationResponseDTO() {}

    public RegistrationResponseDTO(String token, UUID keyID) {
        this.token = token;
        this.keyID = keyID;
    }
}
