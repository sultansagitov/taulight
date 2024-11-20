package net.result.openhelo.messages;

import net.result.sandnode.messages.util.HeadersBuilder;
import org.jetbrains.annotations.NotNull;

import static net.result.openhelo.messages.HeloMessageTypes.FWD;

public class ForwardMessage extends TextMessage {
    public ForwardMessage(@NotNull HeadersBuilder headersBuilder, String data) {
        super(headersBuilder.set(FWD), data);
    }
}
