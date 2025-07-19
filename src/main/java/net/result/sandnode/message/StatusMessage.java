package net.result.sandnode.message;

import net.result.sandnode.message.util.Headers;
import org.jetbrains.annotations.NotNull;

public abstract class StatusMessage extends BaseMessage {
    private final String code;

    public StatusMessage(@NotNull Headers headers, String code) {
        super(headers);
        this.code = code;
    }

    public StatusMessage(@NotNull Message response) {
        super(response.headers());
        code = new String(response.getBody());
    }

    public String code() {
        return code;
    }

    @Override
    public byte[] getBody() {
        return code.getBytes();
    }
}
