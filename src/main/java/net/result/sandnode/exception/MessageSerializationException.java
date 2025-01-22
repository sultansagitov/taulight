package net.result.sandnode.exception;

import net.result.sandnode.message.IMessage;

public class MessageSerializationException extends SandnodeMessageException {
    public MessageSerializationException(IMessage snMessage, String message, Throwable e) {
        super(snMessage, message, e);
    }
}
