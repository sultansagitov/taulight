package net.result.sandnode.message.types;

import net.result.sandnode.message.EmptyMessage;
import net.result.sandnode.message.util.Headers;
import org.jetbrains.annotations.NotNull;

import static net.result.sandnode.message.util.MessageTypes.WARN;

public class WarningMessage extends EmptyMessage {
    public WarningMessage(@NotNull Headers headers) {
        super(headers.setType(WARN));
    }

    public WarningMessage() {
        this(new Headers());
    }
}
