package net.result.taulight.message.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.IMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.MSGPackMessage;

import static net.result.taulight.message.TauMessageTypes.FWD;

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
