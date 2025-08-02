package net.result.sandnode.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RegistrationResponseDTO {
    @JsonProperty
    public String token;

    @SuppressWarnings("unused")
    public RegistrationResponseDTO() {}

    public RegistrationResponseDTO(String token) {
        this.token = token;
    }
}
