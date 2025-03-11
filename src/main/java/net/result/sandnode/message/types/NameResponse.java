package net.result.sandnode.message.types;

import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.TextMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import org.jetbrains.annotations.NotNull;

public class NameResponse extends TextMessage {
    public NameResponse(String name) {
        this(new Headers(), name);
    }

    public NameResponse(@NotNull Headers headers, String name) {
        super(headers.setType(MessageTypes.NAME), name);
    }

    public NameResponse(@NotNull RawMessage raw) throws ExpectedMessageException {
        super(raw.expect(MessageTypes.NAME));
    }
}
