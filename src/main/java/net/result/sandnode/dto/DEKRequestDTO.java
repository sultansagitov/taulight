package net.result.sandnode.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DEKRequestDTO {
    public static class Send {
        @JsonProperty("receiver-nickname")
        public String receiverNickname;

        @JsonProperty("encrypted-key")
        public String encryptedKey;

        @SuppressWarnings("unused")
        public Send() {}

        public Send(String receiverNickname, String encryptedKey) {
            this.receiverNickname = receiverNickname;
            this.encryptedKey = encryptedKey;
        }
    }

    @JsonProperty
    public Send send;

    @JsonProperty
    public boolean get;

    @JsonProperty
    public String getOf;
}
