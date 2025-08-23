package net.result.sandnode.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class DEKDTO {
    @JsonProperty
    public UUID id;

    @JsonProperty("encrypted-key")
    public String encryptedKey;

    public DEKDTO() {}

    public DEKDTO(UUID id, String encryptedKey) {
        this.id = id;
        this.encryptedKey = encryptedKey;
    }
}
