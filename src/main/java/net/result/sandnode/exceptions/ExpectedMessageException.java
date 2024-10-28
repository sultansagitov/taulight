package net.result.sandnode.exceptions;

import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.messages.util.MessageType;
import org.jetbrains.annotations.NotNull;

public class ExpectedMessageException extends Exception {
    public ExpectedMessageException(@NotNull MessageType messageType, RawMessage rawMessage) {
        super("Expected type of message - \"%s\", got \"%s\"".formatted(messageType.name(), rawMessage));
    }
}
