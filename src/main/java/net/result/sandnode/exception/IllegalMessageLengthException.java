package net.result.sandnode.exception;

import net.result.sandnode.message.IMessage;

public class IllegalMessageLengthException extends SandnodeMessageException {
    public IllegalMessageLengthException(IMessage snMessage, int lengthInt) {
        super(snMessage, "Header length exceeds 65535: %d".formatted(lengthInt));
    }
}
