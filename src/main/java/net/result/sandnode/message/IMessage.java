package net.result.sandnode.message;

import net.result.sandnode.exception.*;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.encryption.GlobalKeyStorage;
import net.result.sandnode.encryption.interfaces.IEncryption;
import net.result.sandnode.message.util.MessageType;
import org.jetbrains.annotations.NotNull;

public interface IMessage {

    Headers getHeaders();

    byte[] getBody();

    byte[] toByteArray(@NotNull GlobalKeyStorage globalKeyStorage)
            throws EncryptionException, MessageSerializationException, IllegalMessageLengthException;

    void setHeadersEncryption(@NotNull IEncryption encryption);

    @NotNull IEncryption getHeadersEncryption();

    default @NotNull IMessage expect(MessageType type) throws ExpectedMessageException {
        if (this.getHeaders().getType() != type)
            throw new ExpectedMessageException(type, this);
        return this;
    }
}
