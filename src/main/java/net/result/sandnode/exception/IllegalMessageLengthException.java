package net.result.sandnode.exception;

import net.result.sandnode.message.IMessage;

public class IllegalMessageLengthException extends SandnodeMessageException {
    public IllegalMessageLengthException(IMessage snMessage, String message) {
        super(snMessage, message);
    }
}
