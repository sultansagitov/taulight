package net.result.taulight.message;

import net.result.sandnode.message.EmptyMessage;
import net.result.sandnode.message.util.Headers;
import org.jetbrains.annotations.NotNull;

import static net.result.taulight.message.TauMessageTypes.ONL;

public class OnlineRequestMessage extends EmptyMessage {
    public OnlineRequestMessage() {
        this(new Headers());
    }

    public OnlineRequestMessage(@NotNull Headers headers) {
        super(headers.setType(ONL));
    }
}
