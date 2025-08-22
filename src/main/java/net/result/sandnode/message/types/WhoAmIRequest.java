package net.result.sandnode.message.types;

import net.result.sandnode.message.EmptyMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import org.jetbrains.annotations.NotNull;

public class WhoAmIRequest extends EmptyMessage{


    public WhoAmIRequest() {
        this(new Headers());
    }

    public WhoAmIRequest(@NotNull Headers headers) {
        super(headers.setType(MessageTypes.WHOAMI));
    }

    public WhoAmIRequest(@NotNull RawMessage raw) {
        this(raw.expect(MessageTypes.WHOAMI).headers());
    }

}
