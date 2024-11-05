package net.result.sandnode.messages;

import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.util.encryption.Encryption;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface IMessage {

    HeadersBuilder getHeadersBuilder();

    @NotNull Headers getHeaders();

    byte[] getBody();

    byte[] toByteArray(
            @NotNull GlobalKeyStorage globalKeyStorage,
            @NotNull Encryption encryption
    ) throws IOException, ReadingKeyException, EncryptionException;
}
