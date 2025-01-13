package net.result.taulight.messages.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.exceptions.DeserializationException;
import net.result.sandnode.exceptions.ExpectedMessageException;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.util.Headers;
import net.result.sandnode.messages.MSGPackMessage;

import static net.result.taulight.messages.TauMessageTypes.FWD;

public class ForwardMessage extends MSGPackMessage<ForwardMessage.ForwardData> {
    public static class ForwardData {
        @JsonProperty
        public String content;

        @JsonProperty
        public String chatID;

        public ForwardData() {}
        public ForwardData(String chatID, String data) {
            this.chatID = chatID;
            content = data;
        }
    }

    public ForwardMessage(IMessage request) throws DeserializationException, ExpectedMessageException {
        super(request, ForwardData.class);
        ExpectedMessageException.check(request, FWD);
    }

    public ForwardMessage(Headers headers, ForwardData data) {
        super(headers.setType(FWD), data);
    }

    public ForwardMessage(ForwardData data) {
        this(new Headers(), data);
    }

    public String getData() {
        return object.content;
    }

    public String getChatID() {
        return object.chatID;
    }
}
