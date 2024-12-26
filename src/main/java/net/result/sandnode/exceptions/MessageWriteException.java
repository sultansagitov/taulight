package net.result.sandnode.exceptions;

import net.result.sandnode.messages.IMessage;

public class MessageWriteException extends SandnodeMessageException {
    public MessageWriteException(IMessage snMessage, String message, Throwable e) {
        super(snMessage, message, e);
    }
}
