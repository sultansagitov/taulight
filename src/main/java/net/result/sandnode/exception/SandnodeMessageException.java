package net.result.sandnode.exception;

import net.result.sandnode.message.IMessage;

public class SandnodeMessageException extends SandnodeException {
    public final IMessage snMessage;

    public SandnodeMessageException(IMessage snMessage, String message, Throwable e) {
         super("%s: %s".formatted(message, snMessage.toString()), e);
        this.snMessage = snMessage;
    }

    public SandnodeMessageException(IMessage snMessage, String message) {
        super("%s: %s".formatted(message, snMessage.toString()));
        this.snMessage = snMessage;
    }
}
