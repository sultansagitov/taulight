package net.result.sandnode.message.types;

import net.result.sandnode.message.EmptyMessage;
import net.result.sandnode.message.util.Headers;
import org.jetbrains.annotations.NotNull;

import static net.result.sandnode.message.util.MessageTypes.EXIT;

public class ExitMessage extends EmptyMessage {
    public ExitMessage(@NotNull Headers headers) {
        super(headers.setType(EXIT));
    }

    public ExitMessage() {
        this(new Headers());
    }
}
