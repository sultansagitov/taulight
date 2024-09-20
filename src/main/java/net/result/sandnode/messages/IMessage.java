package net.result.sandnode.messages;

import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.util.encryption.Encryption;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public interface IMessage {

    @NotNull Headers getHeaders();

    byte[] getBody() throws ReadingKeyException, EncryptionException;

    byte[] toByteArray(Encryption encryption, GlobalKeyStorage globalKeyStorage) throws NoSuchEncryptionException, IOException, NoSuchAlgorithmException, ReadingKeyException, EncryptionException;
}
