package net.result.sandnode.message.types;

import net.result.sandnode.message.EmptyMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import org.jetbrains.annotations.NotNull;

public class ExitMessage extends EmptyMessage {
    public ExitMessage(@NotNull Headers headers) {
        super(headers.setType(MessageTypes.EXIT));
    }

    public ExitMessage() {
        this(new Headers());
    }

    public ExitMessage(RawMessage raw) {
        super(raw.expect(MessageTypes.EXIT).headers());
    }
}
