package net.result.taulight.message.types;

import net.result.sandnode.message.EmptyMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.message.TauMessageTypes;
import org.jetbrains.annotations.NotNull;

public class ForwardResponse extends EmptyMessage {
    public ForwardResponse(@NotNull Headers headers) {
        super(headers.setType(TauMessageTypes.FWD));
    }

    public ForwardResponse() {
        this(new Headers());
    }
}
