package net.result.taulight.messages.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.exceptions.DeserializationException;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.util.Headers;
import net.result.sandnode.messages.MSGPackMessage;

import static net.result.taulight.messages.TauMessageTypes.FWD;

public class ForwardMessage extends MSGPackMessage<ForwardMessage.ForwardData> {
    public static class ForwardData {
        @JsonProperty
        public String content;

        public ForwardData() {}
        public ForwardData(String data) {
            content = data;
        }
    }

    public final String data;

    public ForwardMessage(Headers headers, String data) {
        super(headers.setType(FWD), new ForwardData(data));
        this.data = data;
    }

    public ForwardMessage(String data) {
        this(new Headers(), data);
    }

    public ForwardMessage(IMessage request) throws DeserializationException {
        super(request, ForwardData.class);
        data = object.content;
    }
}
