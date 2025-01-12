package net.result.taulight.messages.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import net.result.sandnode.exceptions.DeserializationException;
import net.result.sandnode.exceptions.ExpectedMessageException;
import net.result.sandnode.messages.MSGPackMessage;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.messages.util.Headers;
import net.result.taulight.messages.DataType;

import java.util.Set;

import static net.result.taulight.messages.TauMessageTypes.TAULIGHT;

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
