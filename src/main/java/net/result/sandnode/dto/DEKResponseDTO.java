package net.result.sandnode.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DEKResponseDTO {
    @JsonProperty
    public DEKDTO dek;
    @JsonProperty
    public String senderNickname;

    @SuppressWarnings("unused")
    public DEKResponseDTO() {}

    public DEKResponseDTO(DEKDTO dek, String senderNickname) {
        this.dek = dek;
        this.senderNickname = senderNickname;
    }
}
