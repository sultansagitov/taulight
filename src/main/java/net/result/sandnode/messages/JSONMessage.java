package net.result.sandnode.messages;

import net.result.sandnode.messages.util.HeadersBuilder;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import static java.nio.charset.StandardCharsets.US_ASCII;

public class JSONMessage extends Message implements IJSONMessage {
    protected final JSONObject content;

    public JSONMessage(@NotNull HeadersBuilder builder, @NotNull JSONObject content) {
        super(builder);
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
