package net.result.taulight.message.types;

import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.db.ReactionEntryEntity;
import net.result.taulight.dto.ReactionDTO;
import net.result.taulight.message.TauMessageTypes;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ReactionResponse extends MSGPackMessage<ReactionDTO> {
    private static final String key = "your-session";

    public ReactionResponse(@NotNull Headers headers, ReactionDTO dto, boolean yourSession) {
        super(headers.setValue(key, String.valueOf(yourSession)).setType(TauMessageTypes.REACTION), dto);
    }

    public ReactionResponse(boolean isReact, ReactionEntryEntity entry, boolean yourSession) {
        this(new Headers(), new ReactionDTO(isReact, entry), yourSession);
    }

    public ReactionResponse(
            boolean isReact,
            String nickname,
            UUID chatID,
            UUID messageID,
            String packageName,
            String reaction,
            boolean yourSession
    ) {
        this(new Headers(), new ReactionDTO(isReact, nickname, chatID, messageID, packageName, reaction), yourSession);
    }

    public ReactionResponse(RawMessage raw) throws DeserializationException {
        super(raw, ReactionDTO.class);
    }

    public ReactionDTO getReaction() {
        return object;
    }

    public boolean isYourSession() {
        return headers().getOptionalValue(key).map(Boolean::parseBoolean).orElse(false);
    }
}
