package net.result.sandnode.messages;

import net.result.sandnode.messages.util.Headers;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import static java.nio.charset.StandardCharsets.US_ASCII;

public class JSONMessage extends Message implements IJSONMessage {
    private final JSONObject content;

    public JSONMessage(@NotNull Headers headers, @NotNull JSONObject content) {
        super(headers);
        this.content = content;
    }

    public JSONMessage(@NotNull Headers headers) {
        this(headers, new JSONObject());
    }

    public JSONMessage(@NotNull IMessage message) {
        this(message.getHeaders(), new JSONObject(new String(message.getBody())));
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
