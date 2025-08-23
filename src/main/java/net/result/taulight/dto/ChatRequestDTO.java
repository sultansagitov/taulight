package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
public class ChatRequestDTO {
    @JsonProperty("chat-id-list")
    public @Nullable Collection<UUID> allChatID = null;
    @JsonProperty("properties")
    public Collection<ChatInfoPropDTO> infoProps;

    public ChatRequestDTO(Collection<ChatInfoPropDTO> infoProps) {
        this(null, infoProps);
    }
}
