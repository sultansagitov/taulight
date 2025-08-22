package net.result.sandnode.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RegisterRequestDTO {
    @JsonProperty
    public String nickname;
    @JsonProperty
    public String password;
    @JsonProperty
    public String device;
    @JsonProperty("key-storage")
    public PublicKeyDTO keyStorage;

    @SuppressWarnings("unused")
    public RegisterRequestDTO() {}

    public RegisterRequestDTO(String nickname, String password, String device, PublicKeyDTO keyStorage) {
        this.nickname = nickname;
        this.password = password;
        this.device = device;
        this.keyStorage = keyStorage;
    }
}
