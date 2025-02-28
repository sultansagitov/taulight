package net.result.sandnode.exception;

import net.result.sandnode.message.IMessage;
import org.jetbrains.annotations.NotNull;

public class SandnodeMessageException extends SandnodeException {
    public final IMessage message;

    public SandnodeMessageException(@NotNull IMessage snMessage, String message, Throwable e) {
        super("%s: %s".formatted(message, snMessage), e);
        this.message = snMessage;
    }

    public SandnodeMessageException(@NotNull IMessage snMessage, String message) {
        super("%s: %s".formatted(message, snMessage));
        this.message = snMessage;
    }
}
