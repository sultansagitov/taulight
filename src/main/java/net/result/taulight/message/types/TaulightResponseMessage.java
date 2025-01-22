package net.result.taulight.message.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.message.DataType;
import net.result.taulight.messenger.TauChat;

import java.util.Set;
import java.util.stream.Collectors;

import static net.result.taulight.message.DataType.GET;
import static net.result.taulight.message.TauMessageTypes.TAULIGHT;

public class TaulightResponseMessage extends MSGPackMessage<TaulightResponseMessage.TaulightResponseData> {
    public static class TaulightResponseData {
        @JsonProperty
        DataType messageType;
        @JsonProperty
        Set<String> chats;

        public TaulightResponseData() {}
        public TaulightResponseData(DataType messageType, Set<String> chats) {
            this.messageType = messageType;
            this.chats = chats;
        }

        public TaulightResponseData(DataType messageType) {
            this.messageType = messageType;
        }

        public static TaulightResponseData get(Set<TauChat> chats) {
            return new TaulightResponseData(GET, chats.stream().map(chat -> chat.name).collect(Collectors.toSet()));
        }
    }

    public TaulightResponseMessage(RawMessage raw) throws DeserializationException, ExpectedMessageException {
        super(raw, TaulightResponseData.class);
        ExpectedMessageException.check(raw, TAULIGHT);
    }

    public TaulightResponseMessage(Headers headers, TaulightResponseData data) {
        super(headers.setType(TAULIGHT), data);
    }

    public TaulightResponseMessage(TaulightResponseData data) {
        this(new Headers(), data);
    }

    public Set<String> getChats() {
        return object.chats;
    }
}
