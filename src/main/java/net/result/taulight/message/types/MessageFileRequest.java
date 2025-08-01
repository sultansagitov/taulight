package net.result.taulight.message.types;

import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.message.EmptyMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.message.TauMessageTypes;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class MessageFileRequest extends EmptyMessage {
    public String filename = null;
    public UUID chatID = null;
    public UUID fileID = null;

    public MessageFileRequest(Headers headers) {
        super(headers.setType(TauMessageTypes.MESSAGE_FILE));
    }

    public static MessageFileRequest uploadTo(@NotNull UUID chatID, String name) {
        Headers headers = new Headers()
                .setValue("filename", name)
                .setValue("chat-id", chatID.toString());
        MessageFileRequest request = new MessageFileRequest(headers);
        request.chatID = chatID;
        return request;
    }

    public static MessageFileRequest download(UUID fileID) {
        Headers headers = new Headers().setValue("file-id", fileID.toString());
        MessageFileRequest request = new MessageFileRequest(headers);
        request.fileID = fileID;
        return request;
    }

    public MessageFileRequest(RawMessage raw) throws DeserializationException {
        super(raw.headers());
        Headers headers = headers();
        if (headers.has("chat-id") && headers.has("filename")) {
            chatID = headers.getUUID("chat-id");
            filename = headers.getValue("filename");
        }
        if (headers.has("file-id")) {
            fileID = headers.getUUID("file-id");
        }
    }
}
