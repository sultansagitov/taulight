package net.result.sandnode.exception;

import net.result.sandnode.message.Message;

public class IllegalMessageLengthException extends SandnodeMessageException {
    public IllegalMessageLengthException(Message snMessage, int lengthInt) {
        super(snMessage, "Header length exceeds 65535: %d".formatted(lengthInt));
    }
}
