package net.result.sandnode.message;

import net.result.sandnode.exception.*;
import net.result.sandnode.exception.crypto.CryptoException;
import net.result.sandnode.exception.error.KeyStorageNotFoundException;
import net.result.sandnode.exception.error.EncryptionException;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.encryption.KeyStorageRegistry;
import net.result.sandnode.encryption.interfaces.Encryption;
import net.result.sandnode.message.util.MessageType;
import org.jetbrains.annotations.NotNull;

public interface IMessage {

    Headers headers();

    byte[] getBody();

    byte[] toByteArray(@NotNull KeyStorageRegistry keyStorageRegistry)
            throws EncryptionException, MessageSerializationException, IllegalMessageLengthException, KeyStorageNotFoundException, CryptoException;

    void setHeadersEncryption(@NotNull Encryption encryption);

    @NotNull Encryption headersEncryption();

    default @NotNull IMessage expect(MessageType type) throws ExpectedMessageException {
        if (this.headers().type() != type)
            throw new ExpectedMessageException(type, this);
        return this;
    }
}
