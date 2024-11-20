package net.result.sandnode.messages;

import net.result.sandnode.messages.util.HeadersBuilder;

import static net.result.sandnode.util.encryption.Encryption.NONE;

public class EmptyMessage extends Message {
    public EmptyMessage(HeadersBuilder headersBuilder) {
        super(headersBuilder.set(NONE));
    }

    @Override
    public byte[] getBody() {
        return new byte[0];
    }
}
