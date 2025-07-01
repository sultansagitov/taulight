package net.result.sandnode.message;

import net.result.sandnode.encryption.Encryptions;
import net.result.sandnode.message.util.Headers;
import org.jetbrains.annotations.NotNull;

public class EmptyMessage extends BaseMessage {
    public EmptyMessage(@NotNull Headers headers) {
        super(headers.setBodyEncryption(Encryptions.NONE));
    }

    @Override
    public byte[] getBody() {
        return new byte[0];
    }
}
