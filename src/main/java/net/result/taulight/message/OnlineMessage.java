package net.result.taulight.message;

import net.result.sandnode.message.EmptyMessage;
import net.result.sandnode.message.util.Headers;
import org.jetbrains.annotations.NotNull;

import static net.result.taulight.message.TauMessageTypes.ONL;

public class OnlineMessage extends EmptyMessage {
    public OnlineMessage() {
        this(new Headers());
    }

    public OnlineMessage(@NotNull Headers headers) {
        super(headers.setType(ONL));
    }
}
