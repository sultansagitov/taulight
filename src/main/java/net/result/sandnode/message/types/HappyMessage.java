package net.result.sandnode.message.types;

import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.EmptyMessage;
import net.result.sandnode.message.IMessage;
import net.result.sandnode.message.util.Headers;
import org.jetbrains.annotations.NotNull;

import static net.result.sandnode.message.util.MessageTypes.HAPPY;

public class HappyMessage extends EmptyMessage {
    public HappyMessage() {
        super(new Headers().setType(HAPPY));
    }

    public HappyMessage(@NotNull IMessage message) throws ExpectedMessageException {
        super(message.expect(HAPPY).headers());
    }
}
