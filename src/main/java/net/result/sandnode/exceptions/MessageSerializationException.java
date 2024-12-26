package net.result.sandnode.exceptions;

import net.result.sandnode.messages.IMessage;

public class MessageSerializationException extends SandnodeMessageException {
    public MessageSerializationException(IMessage snMessage, String message, Throwable e) {
        super(snMessage, message, e);
    }
}
