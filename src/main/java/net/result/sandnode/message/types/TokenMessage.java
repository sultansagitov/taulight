package net.result.sandnode.message.types;

import net.result.sandnode.message.IMessage;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.util.Headers;
import org.jetbrains.annotations.NotNull;

public class TokenMessage extends Message {
    private final String token;

    public TokenMessage(@NotNull Headers headers, @NotNull String token) {
        super(headers);
        this.token = token;
    }

    public TokenMessage(@NotNull IMessage message) {
        super(message.headers());
        token = new String(message.getBody());
    }

    public String getToken() {
        return token;
    }

    @Override
    public byte[] getBody() {
        return getToken().getBytes();
    }
}
