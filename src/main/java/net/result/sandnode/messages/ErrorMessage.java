package net.result.sandnode.messages;

import org.jetbrains.annotations.NotNull;

import static net.result.sandnode.messages.util.MessageType.ERR;

public class ErrorMessage extends StatusMessage {
    public ErrorMessage(@NotNull HeadersBuilder headersBuilder, int code) {
        super(headersBuilder.set(ERR), code);
    }
}
