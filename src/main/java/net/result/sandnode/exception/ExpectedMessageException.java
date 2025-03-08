package net.result.sandnode.exception;

import net.result.sandnode.message.IMessage;
import net.result.sandnode.message.util.MessageType;
import org.jetbrains.annotations.NotNull;

public class ExpectedMessageException extends SandnodeMessageException {
    public final MessageType expectedType;

    public ExpectedMessageException(@NotNull MessageType expectedType, IMessage message) {
        super(message, "Expected type of message - \"%s\", got \"%s\"".formatted(expectedType.name(), message));
        this.expectedType = expectedType;
    }
}
