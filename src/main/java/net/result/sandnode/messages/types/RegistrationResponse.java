package net.result.sandnode.messages.types;

import net.result.sandnode.exceptions.ExpectedMessageException;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.util.Headers;
import org.jetbrains.annotations.NotNull;

import static net.result.sandnode.messages.util.MessageType.REG;

public class RegistrationResponse extends TokenMessage {
    public RegistrationResponse(@NotNull IMessage message) throws ExpectedMessageException {
        super(message);
        ExpectedMessageException.check(message, REG);
    }

    public RegistrationResponse(@NotNull Headers headers, @NotNull String token) {
        super(headers.setType(REG), token);
    }
}
