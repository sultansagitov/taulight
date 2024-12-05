package net.result.taulight.messages;

import net.result.sandnode.messages.EmptyMessage;
import net.result.sandnode.messages.util.Headers;
import org.jetbrains.annotations.NotNull;

import static net.result.taulight.messages.TauMessageTypes.ONL;

public class OnlineMessage extends EmptyMessage {
    public OnlineMessage(@NotNull Headers headers) {
        super(headers.set(ONL));
    }
}
