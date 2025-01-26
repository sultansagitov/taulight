package net.result.sandnode.exception;

import net.result.sandnode.message.IMessage;
import net.result.sandnode.message.util.MessageType;

public class ExpectedMessageException extends SandnodeException {
    public ExpectedMessageException(MessageType messageType, IMessage message) {
        super("Expected type of message - \"%s\", got \"%s\"".formatted(messageType.name(), message));
    }
}
