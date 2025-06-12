package net.result.taulight.message.types;

import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.message.EmptyMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.message.TauMessageTypes;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class MessageFileRequest extends EmptyMessage {
    public UUID chatID = null;

    public MessageFileRequest(Headers headers) {
        super(headers.setType(TauMessageTypes.MESSAGE_FILE));
    }

    public static MessageFileRequest uploadTo(@NotNull UUID chatID) {
        Headers headers = new Headers().setValue("chat-id", chatID.toString());
        MessageFileRequest request = new MessageFileRequest(headers);
        request.chatID = chatID;
        return request;
    }

    public MessageFileRequest(RawMessage raw) throws DeserializationException {
        super(raw.headers());
        String chatIDString = null;
        try {
            chatIDString = raw.headers().getValue("chat-id");
        } catch (IllegalArgumentException ignored) {
        }

        try {
            if (chatIDString != null) {
                chatID = UUID.fromString(chatIDString);
            }
        } catch (Exception e) {
            throw new DeserializationException(e);
        }
    }
}
