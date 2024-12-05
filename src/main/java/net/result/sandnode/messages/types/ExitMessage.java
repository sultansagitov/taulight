package net.result.sandnode.messages.types;

import net.result.sandnode.messages.EmptyMessage;
import net.result.sandnode.messages.util.Headers;
import org.jetbrains.annotations.NotNull;

import static net.result.sandnode.messages.util.MessageTypes.EXIT;

public class ExitMessage extends EmptyMessage {
    public ExitMessage(@NotNull Headers headers) {
        super(headers.set(EXIT));
    }

}
