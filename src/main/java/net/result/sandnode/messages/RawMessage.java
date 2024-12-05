package net.result.sandnode.messages;

import net.result.sandnode.messages.util.Headers;
import org.jetbrains.annotations.NotNull;

public class RawMessage extends Message {
    private byte[] body;

    public RawMessage(@NotNull Headers headers) {
        super(headers);
    }

    public RawMessage(@NotNull Headers headers, byte @NotNull [] body) {
        super(headers);
        setBody(body);
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}
