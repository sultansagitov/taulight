package net.result.sandnode.messages;

import net.result.sandnode.exceptions.ReadingKeyException;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import static java.nio.charset.StandardCharsets.US_ASCII;

public class JSONMessage extends Message implements IJSONMessage {
    protected JSONObject content;

    public JSONMessage(@NotNull HeadersBuilder headersBuilder, @NotNull JSONObject content) {
        super(headersBuilder.set("application/json"));
        this.content = content;
    }

    public JSONMessage(@NotNull RawMessage rawMessage) {
        super(rawMessage.getHeadersBuilder().set("application/json"));
        this.content = new JSONObject(new String(rawMessage.getBody()));
    }

    @Override
    public @NotNull JSONObject getContent() {
        return content;
    }

    @Override
    public byte @NotNull [] getBody() throws ReadingKeyException {
        return getContent().toString().getBytes(US_ASCII);
    }
}
