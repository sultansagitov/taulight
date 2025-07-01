package net.result.sandnode.exception;

import net.result.sandnode.message.Message;

public class MessageWriteException extends SandnodeMessageException {
    public MessageWriteException(Message snMessage, String message, Throwable e) {
        super(snMessage, message, e);
    }
}
