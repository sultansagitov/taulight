package net.result.taulight.messages.types;

import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.JSONMessage;
import net.result.sandnode.messages.util.Headers;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import static net.result.taulight.messages.TauMessageTypes.ECHO;

public class EchoMessage extends JSONMessage {
    public final String data;

    public EchoMessage(@NotNull Headers headers, @NotNull String data) {
        super(headers.setType(ECHO), new JSONObject().put("content", data));
        this.data = data;
    }

    public EchoMessage(@NotNull String data) {
        this(new Headers(), data);
    }

    public EchoMessage(IMessage request) {
        super(request);
        this.data = getContent().getString("content");
    }
}
