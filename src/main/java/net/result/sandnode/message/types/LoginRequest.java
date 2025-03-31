package net.result.sandnode.message.types;

import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.TextMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import org.jetbrains.annotations.NotNull;

public class LoginRequest extends TextMessage {
    public LoginRequest(@NotNull Headers headers, String token) {
        super(headers.setType(MessageTypes.LOGIN), token);
    }

    public LoginRequest(String token) {
        this(new Headers(), token);
    }

    public LoginRequest(@NotNull RawMessage request) throws ExpectedMessageException {
        super(request.expect(MessageTypes.LOGIN));
    }
}
