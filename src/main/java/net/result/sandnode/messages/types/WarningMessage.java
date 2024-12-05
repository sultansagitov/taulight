package net.result.sandnode.messages.types;

import net.result.sandnode.messages.Message;
import net.result.sandnode.messages.util.Headers;
import org.jetbrains.annotations.NotNull;

import static net.result.sandnode.messages.util.MessageTypes.WARN;

public class WarningMessage extends Message {
    public WarningMessage(@NotNull Headers headers) {
        super(headers.set(WARN));
    }

    @Override
    public byte[] getBody() {
        return new byte[0];
    }
}
