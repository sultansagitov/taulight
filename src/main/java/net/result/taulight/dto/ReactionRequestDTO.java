package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record ReactionRequestDTO(
    @JsonProperty("message-id") UUID messageID,
    @JsonProperty("reaction") String reaction,
    @JsonProperty("react") boolean react
) {}
