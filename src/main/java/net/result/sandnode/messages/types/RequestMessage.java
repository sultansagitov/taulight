package net.result.sandnode.messages.types;

import net.result.sandnode.messages.EmptyMessage;
import net.result.sandnode.messages.util.Headers;
import org.jetbrains.annotations.NotNull;

import static net.result.sandnode.messages.util.MessageType.REQ;

public class RequestMessage extends EmptyMessage {
    public RequestMessage(@NotNull Headers headers) {
        super(headers.setType(REQ));
    }

    public RequestMessage() {
        this(new Headers());
    }
}
