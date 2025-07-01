package net.result.sandnode.message;

import net.result.sandnode.message.util.Headers;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public abstract class StatusMessage extends BaseMessage {
    private final int code;

    public StatusMessage(@NotNull Headers headers, int code) {
        super(headers);
        this.code = code;
    }

    public StatusMessage(@NotNull Message response) {
        super(response.headers());
        code = ByteBuffer.wrap(response.getBody()).getInt();
    }

    public int code() {
        return code;
    }

    @Override
    public byte[] getBody() {
        return ByteBuffer.allocate(4).putInt(code).array();
    }
}
