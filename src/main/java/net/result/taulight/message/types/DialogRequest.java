package net.result.taulight.message.types;

import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.TextMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.message.TauMessageTypes;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class DialogRequest extends TextMessage {
    public enum Type {ID, AVATAR}

    private DialogRequest(Type type, String string) {
        super(new Headers().setType(TauMessageTypes.DIALOG).setValue("type", type.name()), string);
    }

    public static @NotNull DialogRequest getDialogID(String nickname) {
        return new DialogRequest(Type.ID, nickname);
    }

    public static @NotNull DialogRequest getAvatar(UUID chatID) {
        return new DialogRequest(Type.AVATAR, chatID.toString());
    }

    public DialogRequest(@NotNull RawMessage raw) throws ExpectedMessageException {
        super(raw.expect(TauMessageTypes.DIALOG));
    }
}
