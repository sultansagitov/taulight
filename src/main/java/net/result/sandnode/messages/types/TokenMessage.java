package net.result.sandnode.messages.types;

import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.JSONMessage;
import net.result.sandnode.messages.util.Headers;
import org.jetbrains.annotations.NotNull;

public class TokenMessage extends JSONMessage {
    private final String token;

    public TokenMessage(@NotNull IMessage message) {
        super(message);

        token = getContent().getString("token");
    }

    public TokenMessage(@NotNull Headers headers, @NotNull String token) {
        super(headers);
        this.token = token;
        getContent().put("token", token);
    }

    public String getToken() {
        return token;
    }

}
