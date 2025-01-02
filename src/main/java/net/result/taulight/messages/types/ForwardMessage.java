package net.result.taulight.messages.types;

import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.JSONMessage;
import net.result.sandnode.messages.util.Headers;
import org.json.JSONObject;

import static net.result.taulight.messages.TauMessageTypes.FWD;

public class ForwardMessage extends JSONMessage {
    public final String data;

    public ForwardMessage(Headers headers, String data) {
        super(headers.setType(FWD), new JSONObject().put("content", data));
        this.data = data;
    }

    public ForwardMessage(String data) {
        this(new Headers(), data);
    }

    public ForwardMessage(IMessage request) {
        super(request);
        data = getContent().getString("content");
    }
}
