package net.result.sandnode.exception;

import net.result.sandnode.message.Message;
import net.result.sandnode.message.util.MessageType;
import org.jetbrains.annotations.NotNull;

public class ExpectedMessageException extends ProtocolException {
    public final MessageType expectedType;
    public final Message message;

    public ExpectedMessageException(@NotNull MessageType expectedType, Message message) {
        super("Expected type of message - \"%s\", got \"%s\"".formatted(expectedType.name(), message));
        this.expectedType = expectedType;
        this.message = message;
    }
}
