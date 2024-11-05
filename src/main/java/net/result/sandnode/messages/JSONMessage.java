package net.result.sandnode.messages;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import static java.nio.charset.StandardCharsets.US_ASCII;

public class JSONMessage extends Message implements IJSONMessage {
    protected final JSONObject content;

    public JSONMessage(@NotNull HeadersBuilder headersBuilder, @NotNull JSONObject content) {
        super(headersBuilder.set("application/json"));
        this.content = content;
    }

    @Override
    public @NotNull JSONObject getContent() {
        return content;
    }

    @Override
    public byte @NotNull [] getBody() {
        return getContent().toString().getBytes(US_ASCII);
    }
}
