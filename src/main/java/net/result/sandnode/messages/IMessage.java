package net.result.sandnode.messages;

import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.util.encryption.Encryption;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface IMessage extends IParameters {

    @NotNull Headers getHeaders();

    byte[] getBody() throws ReadingKeyException, EncryptionException;

    byte[] toByteArray(Encryption encryption, GlobalKeyStorage globalKeyStorage) throws IOException, ReadingKeyException, EncryptionException;
}
