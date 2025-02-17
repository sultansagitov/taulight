package net.result.sandnode.exception;

import net.result.sandnode.message.IMessage;
import net.result.sandnode.message.Message;

public class MessageSerializationException extends SandnodeMessageException {
    public MessageSerializationException(IMessage snMessage, String message, Throwable e) {
        super(snMessage, message, e);
    }

    public MessageSerializationException(Message snMessage, Exception e) {
        super(snMessage, "%s: %s".formatted(e.getClass().getSimpleName(), e.getMessage()), e);
    }
}
