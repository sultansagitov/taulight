package net.result.taulight.message.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.IMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.MSGPackMessage;
import net.result.taulight.message.TauMessageTypes;

public class ForwardRequest extends MSGPackMessage<ForwardRequest.ForwardData> {
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

    public ForwardRequest(IMessage request) throws DeserializationException, ExpectedMessageException {
        super(request.expect(TauMessageTypes.FWD_REQ), ForwardData.class);
    }

    public ForwardRequest(Headers headers, ForwardData data) {
        super(headers.setType(TauMessageTypes.FWD_REQ), data);
    }

    public ForwardRequest(ForwardData data) {
        this(new Headers(), data);
    }

    public String getData() {
        return object.content;
    }

    public String getChatID() {
        return object.chatID;
    }
}
