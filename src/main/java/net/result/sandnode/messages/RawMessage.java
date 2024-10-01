package net.result.sandnode.messages;

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

    public void setBody(byte[] body) {
        this.body = body;
    }

    public byte[] getBody() {
        return body;
    }
}
