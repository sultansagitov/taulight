package net.result.sandnode.messages;

import net.result.sandnode.messages.util.HeadersBuilder;
import org.jetbrains.annotations.NotNull;

public class RawMessage extends Message {
    private byte[] body;

    public RawMessage(@NotNull HeadersBuilder headersBuilder) {
        super(headersBuilder);
    }

    public RawMessage(@NotNull HeadersBuilder headersBuilder, byte @NotNull [] body) {
        super(headersBuilder);
        setBody(body);
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}
