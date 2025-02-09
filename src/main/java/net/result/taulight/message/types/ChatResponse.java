package net.result.taulight.message.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.message.TauMessageTypes;
import net.result.taulight.db.TauChat;
import net.result.taulight.message.types.ChatRequest.DataType;

import java.util.Collection;
import java.util.stream.Collectors;

public class ChatResponse extends MSGPackMessage<ChatResponse.Data> {
    protected static class Data {
        @JsonProperty
        ChatRequest.DataType messageType;
        @JsonProperty
        Collection<String> chats;

        @SuppressWarnings("unused")
        public Data() {}
        public Data(ChatRequest.DataType messageType, Collection<String> chats) {
            this.messageType = messageType;
            this.chats = chats;
        }
    }

    public ChatResponse(RawMessage raw) throws DeserializationException, ExpectedMessageException {
        super(raw.expect(TauMessageTypes.CHAT), Data.class);
    }

    private ChatResponse(Headers headers, Data data) {
        super(headers.setType(TauMessageTypes.CHAT), data);
    }

    private ChatResponse(Data data) {
        this(new Headers(), data);
    }

    public static ChatResponse get(Collection<TauChat> chats) {
        return new ChatResponse(new Data(
                DataType.GET,
                chats.stream()
                        .map(TauChat::getID)
                        .collect(Collectors.toSet())
        ));
    }

    public Collection<String> getChats() {
        return object.chats;
    }
}
