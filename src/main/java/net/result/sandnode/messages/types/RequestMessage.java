package net.result.sandnode.messages.types;

import net.result.sandnode.exceptions.ExpectedMessageException;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.JSONMessage;
import net.result.sandnode.messages.util.Headers;
import org.jetbrains.annotations.NotNull;

import static net.result.sandnode.messages.util.MessageTypes.REQ;

public class RequestMessage extends JSONMessage {
    public RequestMessage(@NotNull Headers headers) {
        super(headers.set(REQ));
    }

    public RequestMessage(@NotNull IMessage request) throws ExpectedMessageException {
        super(request.getHeaders());
        ExpectedMessageException.check(request, REQ);
    }
}
