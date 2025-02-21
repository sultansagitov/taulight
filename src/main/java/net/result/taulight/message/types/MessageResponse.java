package net.result.taulight.message.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.db.ChatMessage;
import net.result.taulight.message.TauMessageTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class MessageResponse extends MSGPackMessage<MessageResponse.Data> {
    public static class Data {
        @JsonProperty
        public Collection<ChatMessage> messages;

        @SuppressWarnings("unused")
        public Data() {}

        public Data(Collection<ChatMessage> messages) {
            this.messages = messages;
        }
    }

    public MessageResponse(@NotNull Headers headers, Collection<ChatMessage> messages) {
        super(headers.setType(TauMessageTypes.MESSAGE), new MessageResponse.Data(messages));
    }

    public MessageResponse(Collection<ChatMessage> messages) {
        this(new Headers(), messages);
    }

    public MessageResponse(@NotNull RawMessage raw) throws ExpectedMessageException, DeserializationException {
        super(raw.expect(TauMessageTypes.MESSAGE), MessageResponse.Data.class);
    }

    public Collection<ChatMessage> getMessages() {
        return object.messages;
    }
}
