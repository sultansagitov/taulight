package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class MessageRequestDTO {
    @JsonProperty("chat-id")
    public UUID chatID;
    @JsonProperty
    public int index;
    @JsonProperty
    public int size;

    @SuppressWarnings("unused")
    public MessageRequestDTO() {
    }

    public MessageRequestDTO(UUID chatID, int index, int size) {
        this.chatID = chatID;
        this.index = index;
        this.size = size;
    }
}
