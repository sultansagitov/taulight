package net.result.taulight.message.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.serverclient.ClientMember;
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
        public ZonedDateTime zonedDateTime;
        @JsonProperty
        public ClientMember clientMember;
        @JsonProperty
        public ChatMessage message;

        @SuppressWarnings("unused")
        public Data() {}
        public Data(ZonedDateTime ztd, ClientMember clientMember, ChatMessage message) {
            zonedDateTime = ztd;
            this.clientMember = clientMember;
            this.message = message;
        }
    }

    public ForwardResponse(Headers headers, ChatMessage chatMessage) {
        super(headers.setType(TauMessageTypes.FWD), new Data(
                chatMessage.ztd(),
                new ClientMember(chatMessage.memberID()),
                chatMessage
        ));
    }

    public ForwardResponse(ChatMessage chatMessage) {
        this(new Headers(), chatMessage);
    }

    public ForwardResponse(RawMessage message) throws DeserializationException, ExpectedMessageException {
        super(message.expect(TauMessageTypes.FWD), Data.class);
    }

    public String getData() {
        return object.message.content();
    }

    public String getChatID() {
        return object.message.chatID();
    }

    public ZonedDateTime getZonedDateTime() {
        return object.message.ztd();
    }

    public ClientMember getMember() {
        return new ClientMember(object.message.memberID());
    }
}
