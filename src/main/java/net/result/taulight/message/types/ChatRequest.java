package net.result.taulight.message.types;

import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.dto.ChatInfoPropDTO;
import net.result.taulight.dto.ChatRequestDTO;
import net.result.taulight.message.TauMessageTypes;

import java.util.Collection;
import java.util.UUID;

public class ChatRequest extends MSGPackMessage<ChatRequestDTO> {
    private ChatRequest(Headers headers, ChatRequestDTO data) {
        super(headers.setType(TauMessageTypes.CHAT), data);
    }

    private ChatRequest(ChatRequestDTO data) {
        this(new Headers(), data);
    }

    public ChatRequest(RawMessage raw) throws DeserializationException {
        super(raw, ChatRequestDTO.class);
    }

    public static ChatRequest getByMember(Collection<ChatInfoPropDTO> infoProps) {
        return new ChatRequest(new ChatRequestDTO(infoProps));
    }

    public static ChatRequest getByID(Collection<UUID> chatID, Collection<ChatInfoPropDTO> infoProps) {
        return new ChatRequest(new ChatRequestDTO(chatID, infoProps));
    }
}