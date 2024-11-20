package net.result.sandnode.messages;

import net.result.sandnode.exceptions.KeyStorageNotFoundException;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.messages.util.Headers;
import net.result.sandnode.messages.util.HeadersBuilder;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import net.result.sandnode.util.encryption.interfaces.IEncryption;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface IMessage {

    HeadersBuilder getHeadersBuilder();

    @NotNull Headers getHeaders();

    byte[] getBody();

    byte[] toByteArray(
            @NotNull GlobalKeyStorage globalKeyStorage,
            @NotNull IEncryption encryption
    ) throws IOException, ReadingKeyException, EncryptionException, KeyStorageNotFoundException;
}
