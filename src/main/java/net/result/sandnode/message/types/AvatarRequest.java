package net.result.sandnode.message.types;

import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.EmptyMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class AvatarRequest extends EmptyMessage {
    public enum Type {
        SET("set"),
        GET_MY("get-my"),
        GET_OF("get-of");

        private final String value;

        Type(String value) {
            this.value = value;
        }

        public static Type fromValue(String value) {
            return Arrays.stream(values()).filter(v -> v.value.equalsIgnoreCase(value)).findFirst().orElse(GET_MY);
        }
    }

    public AvatarRequest(@NotNull Headers headers) {
        super(headers.setType(MessageTypes.AVATAR));
    }

    public static AvatarRequest byType(Type type) {
        return new AvatarRequest(new Headers().setValue("type", type.value));
    }

    public AvatarRequest(@NotNull RawMessage raw) throws ExpectedMessageException {
        this(raw.expect(MessageTypes.AVATAR).headers());
    }

    public Type getType() {
        return headers().getOptionalValue("type").map(Type::fromValue).orElse(Type.GET_MY);
    }

    public String getNickname() throws DeserializationException {
        return headers().getValue("nickname");
    }
}
