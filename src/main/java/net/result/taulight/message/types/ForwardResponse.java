package net.result.taulight.message.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.MSGPackMessage;
import net.result.taulight.db.ChatMessage;
import net.result.taulight.message.TauMessageTypes;

import java.time.ZonedDateTime;

public class ForwardResponse extends MSGPackMessage<ForwardResponse.Data> {
    public static class Data {
        @JsonProperty
        public ChatMessage message;
        @JsonProperty
        public ZonedDateTime serverZonedDateTime;

        @SuppressWarnings("unused")
        public Data() {}
        public Data(ChatMessage message, ZonedDateTime ztd) {
            this.message = message;
            serverZonedDateTime = ztd;
        }
    }

    public ForwardResponse(Headers headers, ChatMessage message, ZonedDateTime ztd) {
        super(headers.setType(TauMessageTypes.FWD), new Data(message, ztd));
    }

    public ForwardResponse(ChatMessage chatMessage, ZonedDateTime ztd) {
        this(new Headers(), chatMessage, ztd);
    }

    public ForwardResponse(RawMessage message) throws DeserializationException, ExpectedMessageException {
        super(message.expect(TauMessageTypes.FWD), Data.class);
    }

    public ChatMessage getChatMessage() {
        return object.message;
    }

    public ZonedDateTime getServerZonedDateTime() {
        return object.serverZonedDateTime;
    }
}
