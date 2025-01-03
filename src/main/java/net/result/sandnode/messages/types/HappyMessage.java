package net.result.sandnode.messages.types;

import net.result.sandnode.exceptions.ExpectedMessageException;
import net.result.sandnode.messages.EmptyMessage;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.util.Headers;
import org.jetbrains.annotations.NotNull;

import static net.result.sandnode.messages.util.MessageTypes.HAPPY;

public class HappyMessage extends EmptyMessage {
    public HappyMessage() {
        super(new Headers().setType(HAPPY));
    }

    public HappyMessage(@NotNull IMessage message) throws ExpectedMessageException {
        super(message.getHeaders());
        ExpectedMessageException.check(message, HAPPY);
    }
}
