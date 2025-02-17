package net.result.sandnode.message.types;

import net.result.sandnode.message.EmptyMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import org.jetbrains.annotations.NotNull;

public class WarningMessage extends EmptyMessage {
    public WarningMessage(@NotNull Headers headers) {
        super(headers.setType(MessageTypes.WARN));
    }

    public WarningMessage() {
        this(new Headers());
    }
}
