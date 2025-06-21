package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class ReactionRequestDTO {
    @JsonProperty("message-id")
    public UUID messageID;

    @JsonProperty("reaction")
    public String reaction;

    @JsonProperty("react")
    public boolean react = true;

    @SuppressWarnings("unused")
    public ReactionRequestDTO() {
    }

    public ReactionRequestDTO(UUID messageID, String reaction, boolean react) {
        this.messageID = messageID;
        this.reaction = reaction;
        this.react = react;
    }
}
