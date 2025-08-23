package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
public class MessageRequestDTO {
    @JsonProperty("chat-id")
    public UUID chatID;
    @JsonProperty
    public int index;
    @JsonProperty
    public int size;
}
