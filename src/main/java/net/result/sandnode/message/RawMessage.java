package net.result.sandnode.message;

import net.result.sandnode.message.util.Headers;
import org.jetbrains.annotations.NotNull;

public class RawMessage extends BaseMessage {
    private byte[] body;

    public RawMessage(@NotNull Headers headers) {
        this(headers, new byte[] {});
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
