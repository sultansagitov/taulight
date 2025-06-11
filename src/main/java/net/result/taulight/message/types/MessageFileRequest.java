package net.result.taulight.message.types;

import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.message.EmptyMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.message.TauMessageTypes;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class MessageFileRequest extends EmptyMessage {
    public final UUID chatID;

    public MessageFileRequest(@NotNull UUID chatID) {
        super(new Headers().setType(TauMessageTypes.MESSAGE_FILE).setValue("chat-id", chatID.toString()));
        this.chatID = chatID;
    }

    public MessageFileRequest(RawMessage raw) throws DeserializationException {
        super(raw.headers());
        try {
            chatID = UUID.fromString(raw.headers().getValue("chat-id"));
        } catch (IllegalArgumentException e) {
            throw new DeserializationException(e);
        }
    }
}
