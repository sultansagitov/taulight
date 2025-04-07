package net.result.taulight.message.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.dto.ChatMessageViewDTO;
import net.result.taulight.message.TauMessageTypes;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MessageResponse extends MSGPackMessage<MessageResponse.Data> {
    public static class Data {
        @JsonProperty
        public long count;
        @JsonProperty
        public List<ChatMessageViewDTO> messages;

        @SuppressWarnings("unused")
        public Data() {}

        public Data(long count, List<ChatMessageViewDTO> messages) {
            this.count = count;
            this.messages = messages;
        }
    }

    public MessageResponse(@NotNull Headers headers, long count, List<ChatMessageViewDTO> messages) {
        super(headers.setType(TauMessageTypes.MESSAGE), new MessageResponse.Data(count, messages));
    }

    public MessageResponse(long count, List<ChatMessageViewDTO> messages) {
        this(new Headers(), count, messages);
    }

    public MessageResponse(@NotNull RawMessage raw) throws ExpectedMessageException, DeserializationException {
        super(raw.expect(TauMessageTypes.MESSAGE), Data.class);
    }

    public long getCount() {
        return object.count;
    }

    public List<ChatMessageViewDTO> getMessages() {
        return object.messages;
    }
}
