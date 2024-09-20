package net.result.sandnode.messages;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import static java.nio.charset.StandardCharsets.US_ASCII;

public class StatusMessage extends Message implements IMessage {
    protected int code;

    public StatusMessage(@NotNull HeadersBuilder headersBuilder, int code) {
        super(headersBuilder);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public @NotNull JSONObject getContent() {
        return null;
    }

    @Override
    public byte @NotNull [] getBody() {
        if (!getContent().has("code"))
            getContent().put("code", getCode());
        return getContent().toString().getBytes(US_ASCII);
    }
}
