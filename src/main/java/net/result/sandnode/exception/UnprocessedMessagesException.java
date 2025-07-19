package net.result.sandnode.exception;

import net.result.sandnode.message.RawMessage;
import org.jetbrains.annotations.NotNull;

public class UnprocessedMessagesException extends ProtocolException {
    public final RawMessage raw;

    public UnprocessedMessagesException(@NotNull RawMessage raw) {
        super(raw.toString());
        this.raw = raw;
    }
}
