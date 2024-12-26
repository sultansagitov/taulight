package net.result.sandnode.messages;

import net.result.sandnode.messages.util.Headers;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class StatusMessage extends JSONMessage implements IJSONMessage {
    private final int code;

    public StatusMessage(@NotNull Headers headers, int code) {
        super(headers, new JSONObject().put("code", code));
        this.code = code;
    }

    public StatusMessage(IMessage response) {
        super(response.getHeaders());
        this.code = new JSONObject(new String(response.getBody())).getInt("code");
    }

    public int getCode() {
        return code;
    }
}
