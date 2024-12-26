package net.result.sandnode.messages;

import net.result.sandnode.messages.util.Headers;
import org.jetbrains.annotations.NotNull;

import static net.result.sandnode.encryption.Encryption.NONE;

public class EmptyMessage extends Message {
    public EmptyMessage(@NotNull Headers headers) {
        super(headers.setBodyEncryption(NONE));
    }

    @Override
    public byte[] getBody() {
        return new byte[0];
    }
}
