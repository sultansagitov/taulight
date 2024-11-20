package net.result.openhelo.messages;

import net.result.sandnode.messages.Message;
import net.result.sandnode.messages.util.HeadersBuilder;
import org.jetbrains.annotations.NotNull;

import static net.result.openhelo.messages.HeloMessageTypes.ONL;

public class OnlineMessage extends Message {
    public OnlineMessage(@NotNull HeadersBuilder builder) {
        super(builder.set(ONL));
    }

    @Override
    public byte[] getBody() {
        return new byte[0];
    }
}
