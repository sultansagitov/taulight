package net.result.sandnode.messages;

import org.jetbrains.annotations.NotNull;

public class RawMessage extends Message {
    private byte[] body;

    public RawMessage(@NotNull HeadersBuilder headersBuilder) {
        super(headersBuilder);
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public byte[] getBody() {
        return body;
    }
}
