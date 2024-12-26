package net.result.sandnode.messages.types;

import net.result.sandnode.messages.EmptyMessage;
import net.result.sandnode.messages.util.Headers;
import org.jetbrains.annotations.NotNull;

import static net.result.sandnode.messages.util.MessageType.WARN;

public class WarningMessage extends EmptyMessage {
    public WarningMessage(@NotNull Headers headers) {
        super(headers.setType(WARN));
    }

    public WarningMessage() {
        this(new Headers());
    }
}
