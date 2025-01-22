package net.result.sandnode.message.types;

import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.IMessage;
import net.result.sandnode.message.util.Headers;
import org.jetbrains.annotations.NotNull;

import static net.result.sandnode.message.util.MessageTypes.REG;

public class RegistrationResponse extends TokenMessage {
    public RegistrationResponse(@NotNull IMessage message) throws ExpectedMessageException, DeserializationException {
        super(message);
        ExpectedMessageException.check(message, REG);
    }

    public RegistrationResponse(@NotNull Headers headers, @NotNull String token) {
        super(headers.setType(REG), token);
    }

    public RegistrationResponse(@NotNull String token) {
        this(new Headers(), token);
    }
}
