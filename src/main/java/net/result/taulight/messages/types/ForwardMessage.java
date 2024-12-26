package net.result.taulight.messages.types;

import net.result.sandnode.messages.util.Headers;
import org.jetbrains.annotations.NotNull;

import static net.result.taulight.messages.TauMessageTypes.FWD;

public class ForwardMessage extends TextMessage {
    public ForwardMessage(String data) {
        this(new Headers(), data);
    }

    public ForwardMessage(@NotNull Headers headers, String data) {
        super(headers.setType(FWD), data);
    }
}
