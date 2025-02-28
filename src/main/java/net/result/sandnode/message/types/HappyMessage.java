package net.result.sandnode.message.types;

import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.EmptyMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import org.jetbrains.annotations.NotNull;

public class HappyMessage extends EmptyMessage {
    public HappyMessage() {
        super(new Headers().setType(MessageTypes.HAPPY));
    }

    public HappyMessage(@NotNull RawMessage message) throws ExpectedMessageException {
        super(message.expect(MessageTypes.HAPPY).headers());
    }
}
