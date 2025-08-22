package net.result.sandnode.message.types;

import net.result.sandnode.message.EmptyMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import org.jetbrains.annotations.NotNull;

public class NameRequest extends EmptyMessage {
    public NameRequest() {
        this(new Headers());
    }

    public NameRequest(@NotNull Headers headers) {
        super(headers.setType(MessageTypes.NAME));
    }

    public NameRequest(@NotNull RawMessage raw) {
        super(raw.expect(MessageTypes.NAME).headers());
    }
}
