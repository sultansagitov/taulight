package net.result.sandnode.message.types;

import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.EmptyMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class WhoAmIRequest extends EmptyMessage {
    public enum Type {
        NICKNAME("nickname"),
        SET_AVATAR("set-avatar"),
        GET_AVATAR("get-avatar");

        private final String value;

        Type(String value) {
            this.value = value;
        }

        public static Type fromValue(String value) {
            return Arrays.stream(values()).filter(v -> v.value.equalsIgnoreCase(value)).findFirst().orElse(NICKNAME);
        }
    }

    public WhoAmIRequest(@NotNull Headers headers) {
        super(headers.setType(MessageTypes.WHOAMI));
    }

    public static WhoAmIRequest byType(Type type) {
        return new WhoAmIRequest(new Headers().setValue("type", type.value));
    }

    public WhoAmIRequest(@NotNull RawMessage raw) throws ExpectedMessageException {
        this(raw.expect(MessageTypes.WHOAMI).headers());
    }

    public Type getType() {
        return headers().getOptionalValue("type").map(Type::fromValue).orElse(Type.NICKNAME);
    }
}
