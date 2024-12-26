package net.result.sandnode.exceptions;

import net.result.sandnode.messages.IMessage;

public class SandnodeMessageException extends SandnodeException {
    public final IMessage snMessage;

    public SandnodeMessageException(IMessage snMessage, String message, Throwable e) {
        super(message, e);
        this.snMessage = snMessage;
    }

    public SandnodeMessageException(IMessage snMessage, String message) {
        super(message);
        this.snMessage = snMessage;
    }
}
