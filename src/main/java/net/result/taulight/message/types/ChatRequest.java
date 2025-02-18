package net.result.taulight.message.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.message.TauMessageTypes;

import java.util.Collection;
import java.util.UUID;

public class ChatRequest extends MSGPackMessage<ChatRequest.Data> {
    public enum DataType {GET, INFO}

    protected static class Data {
        @JsonProperty("type")
        public DataType dataType;
        @JsonProperty("chat-id-list")
        public Collection<UUID> allChatID;

        @SuppressWarnings("unused")
        public Data() {}
        public Data(DataType dataType) {
            this.dataType = dataType;
        }
        public Data(DataType dataType, Collection<UUID> allChatID) {
            this(dataType);
            this.allChatID = allChatID;
        }
    }

    private ChatRequest(Headers headers, Data data) {
        super(headers.setType(TauMessageTypes.CHAT), data);
    }

    private ChatRequest(Data data) {
        this(new Headers(), data);
    }

    public ChatRequest(RawMessage raw) throws DeserializationException {
        super(raw, Data.class);
    }

    public static ChatRequest get() {
        return new ChatRequest(new Data(DataType.GET));
    }

    public static ChatRequest info(Collection<UUID> chatID) {
        return new ChatRequest(new Data(DataType.INFO, chatID));
    }

    public Collection<UUID> getAllChatID() {
        return object.allChatID;
    }

    public DataType getMessageType() {
        return object.dataType;
    }
}