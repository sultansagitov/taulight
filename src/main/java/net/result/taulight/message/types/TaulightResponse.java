package net.result.taulight.message.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.message.TauMessageTypes;
import net.result.taulight.messenger.TauChat;

import java.util.Collection;
import java.util.stream.Collectors;

public class TaulightResponse extends MSGPackMessage<TaulightResponse.TaulightResponseData> {
    public static class TaulightResponseData {
        @JsonProperty
        TaulightRequest.DataType messageType;
        @JsonProperty
        Collection<String> chats;

        @SuppressWarnings("unused")
        public TaulightResponseData() {}
        public TaulightResponseData(TaulightRequest.DataType messageType, Collection<String> chats) {
            this.messageType = messageType;
            this.chats = chats;
        }

        public static TaulightResponseData get(Collection<TauChat> chats) {
            return new TaulightResponseData(TaulightRequest.DataType.GET,
                    chats.stream()
                            .map(TauChat::getID)
                            .collect(Collectors.toSet()));
        }
    }

    public TaulightResponse(RawMessage raw) throws DeserializationException, ExpectedMessageException {
        super(raw.expect(TauMessageTypes.TAULIGHT), TaulightResponseData.class);
    }

    public TaulightResponse(Headers headers, TaulightResponseData data) {
        super(headers.setType(TauMessageTypes.TAULIGHT), data);
    }

    public TaulightResponse(TaulightResponseData data) {
        this(new Headers(), data);
    }

    public Collection<String> getChats() {
        return object.chats;
    }
}
