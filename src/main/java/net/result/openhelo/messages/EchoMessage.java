package net.result.openhelo.messages;

import net.result.sandnode.messages.util.HeadersBuilder;
import org.jetbrains.annotations.NotNull;

import static net.result.openhelo.messages.HeloMessageTypes.ECH;

public class EchoMessage extends TextMessage {
    public EchoMessage(@NotNull HeadersBuilder headersBuilder, @NotNull String data) {
        super(headersBuilder.set(ECH), data);
    }
}
