package net.result.taulight.message.types;

import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.TextMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.message.TauMessageTypes;
import org.jetbrains.annotations.NotNull;

public class CheckCodeRequest extends TextMessage {
    public CheckCodeRequest(String code) {
        this(new Headers(), code);
    }

    public CheckCodeRequest(@NotNull Headers headers, String code) {
        super(headers.setType(TauMessageTypes.CHECK_CODE), code);
    }

    public CheckCodeRequest(@NotNull RawMessage raw) throws ExpectedMessageException {
        super(raw.expect(TauMessageTypes.CHECK_CODE));
    }
}
