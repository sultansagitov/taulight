package net.result.sandnode.exceptions;

import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.util.IMessageType;

public class ExpectedMessageException extends SandnodeException {
    public ExpectedMessageException(IMessageType messageType, IMessage message) {
        super(String.format("Expected type of message - \"%s\", got \"%s\"", messageType.name(), message));
    }

    public ExpectedMessageException(
            IMessageType messageType1,
            IMessageType messageType2,
            IMessage message
    ) {
        super(String.format("Expected type of message - \"%s\" or \"%s\", got \"%s\"", messageType1.name(), messageType2.name(), message));
    }

    public static void check(IMessage req, IMessageType messageType) throws ExpectedMessageException {
        if (req.getHeaders().getType() != messageType)
            throw new ExpectedMessageException(messageType, req);
    }
}
