package net.result.taulight.messages.types;

import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.Message;
import net.result.sandnode.messages.util.Headers;
import org.jetbrains.annotations.NotNull;

public class TextMessage extends Message {
    public final String data;

    public TextMessage(@NotNull Headers headers, @NotNull String data) {
        super(headers);
        this.data = data;
    }

    public TextMessage(@NotNull IMessage message) {
        this(message.getHeaders(), new String(message.getBody()));
    }

    @Override
    public byte[] getBody() {
        return data.getBytes();
    }
}
