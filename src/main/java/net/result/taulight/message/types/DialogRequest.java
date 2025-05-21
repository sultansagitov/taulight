package net.result.taulight.message.types;

import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.TextMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.message.TauMessageTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.UUID;

public class DialogRequest extends TextMessage {
    public enum Type {
        ID("id"),
        AVATAR("avatar"),
        KEY("key");

        private final String value;

        Type(String value) {
            this.value = value;
        }

        public static Type fromValue(String value) {
            return Arrays.stream(values()).filter(v -> v.value.equalsIgnoreCase(value)).findFirst().orElse(ID);
        }
    }


    private DialogRequest(Type type, String string) {
        super(new Headers().setType(TauMessageTypes.DIALOG).setValue("type", type.value), string);
    }

    public static @NotNull DialogRequest getDialogID(String nickname) {
        return new DialogRequest(Type.ID, nickname);
    }

    public static @NotNull DialogRequest getAvatar(UUID chatID) {
        return new DialogRequest(Type.AVATAR, chatID.toString());
    }

    public static @NotNull DialogRequest getKey(UUID chatID) {
        return new DialogRequest(Type.KEY, chatID.toString());
    }

    public DialogRequest(@NotNull RawMessage raw) throws ExpectedMessageException {
        super(raw.expect(TauMessageTypes.DIALOG));
    }
}
