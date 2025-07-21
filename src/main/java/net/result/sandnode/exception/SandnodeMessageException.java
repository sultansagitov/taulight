package net.result.sandnode.exception;

import net.result.sandnode.message.Message;
import org.jetbrains.annotations.NotNull;

public class SandnodeMessageException extends SandnodeException {
    public final Message message;

    public SandnodeMessageException(@NotNull Message snMessage, String message) {
        super("%s: %s".formatted(message, snMessage));
        this.message = snMessage;
    }
}
