package net.result.sandnode.message.types;

import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import org.jetbrains.annotations.NotNull;

public class RegistrationResponse extends TokenMessage {
    public RegistrationResponse(@NotNull RawMessage message) throws ExpectedMessageException {
        super(message.expect(MessageTypes.REG));
    }

    public RegistrationResponse(@NotNull Headers headers, @NotNull String token) {
        super(headers.setType(MessageTypes.REG), token);
    }

    public RegistrationResponse(@NotNull String token) {
        this(new Headers(), token);
    }
}
