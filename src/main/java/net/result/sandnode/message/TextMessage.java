package net.result.sandnode.message;

import net.result.sandnode.message.util.Headers;
import org.jetbrains.annotations.NotNull;

public class TextMessage extends BaseMessage {
    private final String content;

    public TextMessage(@NotNull Headers headers, String content) {
        super(headers);
        this.content = content;
    }

    public TextMessage(@NotNull Message raw) {
        super(raw.headers());
        this.content = new String(raw.getBody());
    }

    @Override
    public byte[] getBody() {
        return content.getBytes();
    }

    public String content() {
        return content;
    }
}
