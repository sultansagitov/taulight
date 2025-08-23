package net.result.taulight.message.types;

import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.dto.ReactionRequestDTO;
import net.result.taulight.message.TauMessageTypes;

import java.util.UUID;

public class ReactionRequest extends MSGPackMessage<ReactionRequestDTO> {
    private ReactionRequest(Headers headers, ReactionRequestDTO data) {
        super(headers.setType(TauMessageTypes.REACTION), data);
    }

    private ReactionRequest(ReactionRequestDTO data) {
        this(new Headers(), data);
    }

    public ReactionRequest(RawMessage raw) {
        super(raw.expect(TauMessageTypes.REACTION), ReactionRequestDTO.class);
    }

    public static ReactionRequest react(UUID messageID, String reaction) {
        return new ReactionRequest(new ReactionRequestDTO(messageID, reaction, true));
    }

    public static ReactionRequest unreact(UUID messageID, String reaction) {
        return new ReactionRequest(new ReactionRequestDTO(messageID, reaction, false));
    }
}