package net.result.sandnode.exceptions;

import net.result.sandnode.messages.IMessage;

public class IllegalMessageLengthException extends SandnodeMessageException {
    public IllegalMessageLengthException(IMessage snMessage, String message) {
        super(snMessage, message);
    }
}
