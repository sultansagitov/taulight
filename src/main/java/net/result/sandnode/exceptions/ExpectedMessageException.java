package net.result.sandnode.exceptions;

import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.util.MessageType;

public class ExpectedMessageException extends SandnodeException {
    public ExpectedMessageException(MessageType messageType, IMessage message) {
        super("Expected type of message - \"%s\", got \"%s\"".formatted(messageType.name(), message));
    }

    public static void check(IMessage req, MessageType messageType) throws ExpectedMessageException {
        if (req.getHeaders().getType() != messageType)
            throw new ExpectedMessageException(messageType, req);
    }
}
