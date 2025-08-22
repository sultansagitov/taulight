package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

public class ChatRequestDTO {
    @JsonProperty("chat-id-list")
    public @Nullable Collection<UUID> allChatID = null;
    @JsonProperty("properties")
    public Collection<ChatInfoPropDTO> infoProps;

    @SuppressWarnings("unused")
    public ChatRequestDTO() {}

    public ChatRequestDTO(Collection<ChatInfoPropDTO> infoProps) {
        this.infoProps = infoProps;
    }

    public ChatRequestDTO(@NotNull Collection<UUID> allChatID, Collection<ChatInfoPropDTO> infoProps) {
        this.allChatID = allChatID;
        this.infoProps = infoProps;
    }
}
