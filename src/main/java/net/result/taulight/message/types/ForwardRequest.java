package net.result.taulight.message.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.IMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.MSGPackMessage;
import net.result.taulight.message.TauMessageTypes;

public class ForwardRequest extends MSGPackMessage<ForwardRequest.Data> {
    public static class Data {
        @JsonProperty
        public String content;
        @JsonProperty
        public String chatID;

        @SuppressWarnings("unused")
        public Data() {}
        public Data(String chatID, String data) {
            this.chatID = chatID;
            content = data;
        }
    }

    public ForwardRequest(IMessage request) throws DeserializationException, ExpectedMessageException {
        super(request.expect(TauMessageTypes.FWD_REQ), Data.class);
    }

    public ForwardRequest(Headers headers, Data data) {
        super(headers.setType(TauMessageTypes.FWD_REQ), data);
    }

    public ForwardRequest(Data data) {
        this(new Headers(), data);
    }

    public String getData() {
        return object.content;
    }

    public String getChatID() {
        return object.chatID;
    }
}
