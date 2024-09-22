package net.result.sandnode.messages;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class StatusMessage extends JSONMessage implements IJSONMessage {
    protected int code;

    public StatusMessage(@NotNull HeadersBuilder headersBuilder, int code) {
        super(headersBuilder, new JSONObject().put("code", code));
        this.code = code;
    }

    public int getCode() {
        return code;
    }

}
