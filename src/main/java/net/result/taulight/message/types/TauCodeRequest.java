package net.result.taulight.message.types;

import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.TextMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.message.TauMessageTypes;
import org.jetbrains.annotations.NotNull;

public class TauCodeRequest extends TextMessage {
    public TauCodeRequest(@NotNull Headers headers, String code) {
        super(headers.setType(TauMessageTypes.CODE), code);
    }

    public TauCodeRequest(RawMessage raw) {
        super(raw);
    }

    public static @NotNull TauCodeRequest check(String code) {
        return new TauCodeRequest(new Headers().setValue("mode", "check"), code);
    }

    public static @NotNull TauCodeRequest use(String code) {
        return new TauCodeRequest(new Headers().setValue("mode", "use"), code);
    }
}
