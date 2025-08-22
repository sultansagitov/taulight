package net.result.sandnode.message;

import net.result.sandnode.exception.*;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.encryption.interfaces.Encryption;
import net.result.sandnode.message.util.MessageType;
import org.jetbrains.annotations.NotNull;

public interface Message {

    Headers headers();

    byte[] getBody();

    void setHeadersEncryption(@NotNull Encryption encryption);

    @NotNull Encryption headersEncryption();

    default @NotNull Message expect(MessageType type) {
        if (this.headers().type() != type)
            throw new ExpectedMessageException(type, this);
        return this;
    }
}
