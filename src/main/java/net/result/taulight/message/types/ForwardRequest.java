package net.result.taulight.message.types;

import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.EmptyMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.message.TauMessageTypes;
import org.jetbrains.annotations.NotNull;

public class ForwardRequest extends EmptyMessage {
    public ForwardRequest(@NotNull Headers headers) {
        super(headers.setType(TauMessageTypes.FWD_REQ));
    }

    public ForwardRequest() {
        this(new Headers());
    }

    public ForwardRequest(@NotNull RawMessage request) throws ExpectedMessageException {
        super(request.expect(TauMessageTypes.FWD_REQ).getHeaders());
    }
}
