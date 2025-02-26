package net.result.taulight.message.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.message.TauMessageTypes;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class MessageRequest extends MSGPackMessage<MessageRequest.Data> {
    public static class Data {
        @JsonProperty("chat-id")
        public UUID chatID;
        @JsonProperty
        private int index;
        @JsonProperty
        private int size;

        @SuppressWarnings("unused")
        public Data() {}

        public Data(UUID chatID, int index, int size) {
            this.chatID = chatID;
            this.index = index;
            this.size = size;
        }
    }

    public MessageRequest(@NotNull Headers headers, UUID chatID, int index, int size) {
        super(headers.setType(TauMessageTypes.MESSAGE), new Data(chatID, index, size));
    }

    public MessageRequest(UUID chatID, int index, int size) {
        this(new Headers(), chatID, index, size);
    }

    public MessageRequest(@NotNull RawMessage raw) throws ExpectedMessageException, DeserializationException {
        super(raw.expect(TauMessageTypes.MESSAGE), Data.class);
    }

    public UUID getChatID() {
        return object.chatID;
    }

    public int getIndex() {
        return object.index;
    }

    public int getSize() {
        return object.size;
    }
}
