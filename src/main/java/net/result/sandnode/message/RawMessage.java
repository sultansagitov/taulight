package net.result.sandnode.message;

import lombok.Getter;
import lombok.Setter;
import net.result.sandnode.message.util.Headers;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class RawMessage extends BaseMessage {
    private byte[] body;

    public RawMessage(@NotNull Headers headers) {
        this(headers, new byte[] {});
    }

    public RawMessage(@NotNull Headers headers, byte @NotNull [] body) {
        super(headers);
        setBody(body);
    }
}
