package net.result.sandnode.messages;

import net.result.sandnode.exceptions.*;
import net.result.sandnode.messages.util.Headers;
import net.result.sandnode.encryption.GlobalKeyStorage;
import net.result.sandnode.encryption.interfaces.IEncryption;
import org.jetbrains.annotations.NotNull;

public interface IMessage {

    Headers getHeaders();

    byte[] getBody();

    byte[] toByteArray(@NotNull GlobalKeyStorage globalKeyStorage)
            throws EncryptionException, MessageSerializationException, IllegalMessageLengthException;

    void setHeadersEncryption(@NotNull IEncryption encryption);

    @NotNull IEncryption getHeadersEncryption();
}
