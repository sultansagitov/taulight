package net.result.sandnode.messages.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.exceptions.DeserializationException;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.MSGPackMessage;
import net.result.sandnode.messages.util.Headers;
import org.jetbrains.annotations.NotNull;

public class TokenMessage extends MSGPackMessage<TokenMessage.TokenData> {
    public static class TokenData {
        @JsonProperty
        public String token;

        public TokenData() {}
        public TokenData(String token) {
            this.token = token;
        }
    }

    public TokenMessage(@NotNull IMessage message) throws DeserializationException {
        super(message, TokenData.class);
    }

    public TokenMessage(@NotNull Headers headers, @NotNull String token) {
        super(headers, new TokenData(token));
    }

    public String getToken() {
        return object.token;
    }
}
