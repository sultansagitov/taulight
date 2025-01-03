package net.result.sandnode.messages.types;

import net.result.sandnode.exceptions.ExpectedMessageException;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.util.Headers;

import static net.result.sandnode.messages.util.MessageTypes.LOGIN;

public class LoginRequest extends TokenMessage {
    public LoginRequest(Headers headers, String token) {
        super(headers.setType(LOGIN), token);
    }

    public LoginRequest(String token) {
        this(new Headers(), token);
    }

    public LoginRequest(IMessage request) throws ExpectedMessageException {
        super(request);
        ExpectedMessageException.check(request, LOGIN);
    }
}
