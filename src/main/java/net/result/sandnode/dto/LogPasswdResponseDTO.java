package net.result.sandnode.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LogPasswdResponseDTO {
    @JsonProperty
    public String token;

    @SuppressWarnings("unused")
    public LogPasswdResponseDTO() {}

    public LogPasswdResponseDTO(String token) {
        this.token = token;
    }
}
