package net.result.sandnode.messages.types;

import net.result.sandnode.messages.StatusMessage;
import net.result.sandnode.messages.util.Headers;
import org.jetbrains.annotations.NotNull;

import static net.result.sandnode.messages.util.MessageTypes.ERR;

public class ErrorMessage extends StatusMessage {
    public ErrorMessage(@NotNull Headers headers, int code) {
        super(headers.set(ERR), code);
    }
}
