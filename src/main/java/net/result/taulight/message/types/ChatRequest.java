package net.result.taulight.message.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;

import static net.result.taulight.message.TauMessageTypes.CHAT;

public class ChatRequest extends MSGPackMessage<ChatRequest.Data> {
    public enum DataType {GET, REMOVE}

    protected static class Data {
        @JsonProperty
        public DataType dataType;
        @JsonProperty
        public String chatID;

        @SuppressWarnings("unused")
        public Data() {}
        public Data(DataType dataType) {
            this.dataType = dataType;
        }
    }

    private ChatRequest(Headers headers, Data data) {
        super(headers.setType(CHAT), data);
    }

    private ChatRequest(Data data) {
        this(new Headers(), data);
    }

    public ChatRequest(DataType data) {
        this(new Data(data));
    }

    public ChatRequest(RawMessage raw) throws DeserializationException {
        super(raw, Data.class);
    }

    public String getChatID() {
        return object.chatID;
    }

    public DataType getMessageType() {
        return object.dataType;
    }
}