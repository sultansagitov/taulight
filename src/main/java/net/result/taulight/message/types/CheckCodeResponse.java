package net.result.taulight.message.types;

import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.code.TauCode;
import net.result.taulight.message.TauMessageTypes;
import org.jetbrains.annotations.NotNull;

public class CheckCodeResponse extends MSGPackMessage<TauCode> {
    public CheckCodeResponse(TauCode data) {
        this(new Headers(), data);
    }

    public CheckCodeResponse(@NotNull Headers headers, TauCode data) {
        super(headers.setType(TauMessageTypes.CHECK_CODE), data);
    }

    public CheckCodeResponse(@NotNull RawMessage raw) throws DeserializationException, ExpectedMessageException {
        super(raw.expect(TauMessageTypes.CHECK_CODE), TauCode.class);
    }

    public TauCode getCode() {
        return object;
    }
}
