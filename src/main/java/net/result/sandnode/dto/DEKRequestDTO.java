package net.result.sandnode.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

public class DEKRequestDTO {
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Send {
        @JsonProperty("receiver-nickname")
        public String receiverNickname;

        @JsonProperty("encrypted-key")
        public String encryptedKey;
    }

    @JsonProperty
    public Send send;

    @JsonProperty
    public boolean get;

    @JsonProperty
    public String getOf;
}
