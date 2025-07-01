package net.result.sandnode.exception;

import net.result.sandnode.message.Message;
import net.result.sandnode.message.BaseMessage;

public class MessageSerializationException extends SandnodeMessageException {
    public MessageSerializationException(Message snMessage, String message, Throwable e) {
        super(snMessage, message, e);
    }

    public MessageSerializationException(BaseMessage snMessage, Exception e) {
        super(snMessage, "%s: %s".formatted(e.getClass().getSimpleName(), e.getMessage()), e);
    }
}
