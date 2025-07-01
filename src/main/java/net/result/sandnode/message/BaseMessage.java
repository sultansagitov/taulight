package net.result.sandnode.message;

import net.result.sandnode.encryption.Encryptions;
import net.result.sandnode.encryption.KeyStorageRegistry;
import net.result.sandnode.encryption.interfaces.Encryption;
import net.result.sandnode.exception.IllegalMessageLengthException;
import net.result.sandnode.exception.MessageSerializationException;
import net.result.sandnode.exception.crypto.CryptoException;
import net.result.sandnode.exception.error.EncryptionException;
import net.result.sandnode.exception.error.KeyStorageNotFoundException;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.util.MessageUtil;
import org.jetbrains.annotations.NotNull;

public abstract class BaseMessage implements Message {
    private Encryption headersEncryption = Encryptions.NONE;
    private final Headers headers;

    protected BaseMessage(@NotNull Headers headers) {
        this.headers = headers.copy();
    }

    @Override
    public void setHeadersEncryption(@NotNull Encryption encryption) {
        headersEncryption = encryption;
    }

    @Override
    public @NotNull Encryption headersEncryption() {
        return headersEncryption;
    }

    @Override
    public byte[] toByteArray(@NotNull KeyStorageRegistry keyStorageRegistry)
            throws EncryptionException, MessageSerializationException, IllegalMessageLengthException,
            KeyStorageNotFoundException, CryptoException {
        return MessageUtil.toByteArray(this, keyStorageRegistry);
    }

    @Override
    public Headers headers() {
        return headers;
    }

    @Override
    public String toString() {
        try {
            return "<%s %s(headers %s %s cid=%04X %s) %s(body %d)>".formatted(
                    getClass().getSimpleName(),
                    headersEncryption(),
                    headers().type().name(),
                    headers().connection().name(),
                    headers().chainID(),
                    headers().keys(),
                    headers().bodyEncryption().name(),
                    getBody().length
            );
        } catch (NullPointerException e) {
            return "<%s>".formatted(getClass().getSimpleName());
        }
    }
}
