package net.result.sandnode.exception;

import net.result.sandnode.message.IMessage;

public class MessageWriteException extends SandnodeMessageException {
    public MessageWriteException(IMessage snMessage, String message, Throwable e) {
        super(snMessage, message, e);
    }
}
