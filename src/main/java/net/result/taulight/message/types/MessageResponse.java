package net.result.taulight.message.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.db.ServerChatMessage;
import net.result.taulight.message.TauMessageTypes;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MessageResponse extends MSGPackMessage<MessageResponse.Data> {
    public static class Data {
        @JsonProperty
        public List<ServerChatMessage> messages;

        @SuppressWarnings("unused")
        public Data() {}

        public Data(List<ServerChatMessage> messages) {
            this.messages = messages;
        }
    }

    public MessageResponse(@NotNull Headers headers, List<ServerChatMessage> messages) {
        super(headers.setType(TauMessageTypes.MESSAGE), new MessageResponse.Data(messages));
    }

    public MessageResponse(List<ServerChatMessage> messages) {
        this(new Headers(), messages);
    }

    public MessageResponse(@NotNull RawMessage raw) throws ExpectedMessageException, DeserializationException {
        super(raw.expect(TauMessageTypes.MESSAGE), MessageResponse.Data.class);
    }

    public List<ServerChatMessage> getMessages() {
        return object.messages;
    }
}
