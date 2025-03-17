package net.result.taulight.message.types;

import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.code.TauCode;
import net.result.taulight.message.TauMessageTypes;
import org.jetbrains.annotations.NotNull;

public class TauCodeResponse extends MSGPackMessage<TauCode> {
    public TauCodeResponse(TauCode data) {
        this(new Headers(), data);
    }

    public TauCodeResponse(@NotNull Headers headers, TauCode data) {
        super(headers.setType(TauMessageTypes.CODE), data);
    }

    public TauCodeResponse(@NotNull RawMessage raw) throws DeserializationException, ExpectedMessageException {
        super(raw.expect(TauMessageTypes.CODE), TauCode.class);
    }
}
