package net.result.taulight.messages.types;

import net.result.sandnode.messages.util.Headers;
import org.jetbrains.annotations.NotNull;

import static net.result.taulight.messages.TauMessageTypes.ECHO;

public class EchoMessage extends TextMessage {
    public EchoMessage(@NotNull String data) {
        this(new Headers(), data);
    }

    public EchoMessage(@NotNull Headers headers, @NotNull String data) {
        super(headers.setType(ECHO), data);
    }
}
