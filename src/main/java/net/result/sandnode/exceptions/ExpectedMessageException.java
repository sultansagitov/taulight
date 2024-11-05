package net.result.sandnode.exceptions;

import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.messages.util.MessageType;
import org.jetbrains.annotations.NotNull;

public class ExpectedMessageException extends SandnodeException {
    public ExpectedMessageException(@NotNull MessageType messageType, RawMessage rawMessage) {
        super(String.format("Expected type of message - \"%s\", got \"%s\"", messageType.name(), rawMessage));
    }
}
