package net.result.openhelo.messages;

import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.Message;
import net.result.sandnode.messages.util.HeadersBuilder;
import org.jetbrains.annotations.NotNull;

public class TextMessage extends Message {
    public final String data;

    public TextMessage(@NotNull HeadersBuilder headersBuilder, @NotNull String data) {
        super(headersBuilder);
        this.data = data;
    }

    public TextMessage(@NotNull IMessage message) {
        this(message.getHeadersBuilder(), new String(message.getBody()));
    }

    @Override
    public byte[] getBody() {
        return data.getBytes();
    }
}
