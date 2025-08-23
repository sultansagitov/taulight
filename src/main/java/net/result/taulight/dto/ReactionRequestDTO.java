package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
public class ReactionRequestDTO {
    @JsonProperty("message-id")
    public UUID messageID;

    @JsonProperty("reaction")
    public String reaction;

    @JsonProperty("react")
    public boolean react = true;
}
