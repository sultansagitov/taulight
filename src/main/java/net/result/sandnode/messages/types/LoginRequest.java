package net.result.sandnode.messages.types;

import net.result.sandnode.messages.util.Headers;

import static net.result.sandnode.messages.util.MessageTypes.LOGIN;

public class LoginRequest extends TokenMessage {
    public LoginRequest(Headers headers, String token) {
        super(headers.setType(LOGIN), token);
    }

    public LoginRequest(String token) {
        this(new Headers(), token);
    }
}
