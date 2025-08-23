package net.result.sandnode.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDTO {
    @JsonProperty
    public String nickname;
    @JsonProperty
    public String password;
    @JsonProperty
    public String device;
    @JsonProperty("key-storage")
    public PublicKeyDTO keyStorage;
}
