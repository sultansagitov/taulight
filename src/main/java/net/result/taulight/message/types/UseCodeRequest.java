package net.result.taulight.message.types;

import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.TextMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.message.TauMessageTypes;
import org.jetbrains.annotations.NotNull;

public class UseCodeRequest extends TextMessage {
    public UseCodeRequest(String code) {
        this(new Headers(), code);
    }

    public UseCodeRequest(@NotNull Headers headers, String code) {
        super(headers.setType(TauMessageTypes.USE_CODE), code);
    }

    public UseCodeRequest(@NotNull RawMessage raw) throws ExpectedMessageException {
        super(raw.expect(TauMessageTypes.USE_CODE));
    }
}
