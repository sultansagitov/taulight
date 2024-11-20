package net.result.sandnode.util.encryption.interfaces;

import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import org.jetbrains.annotations.NotNull;

public interface IEncryptor {

    byte[] encrypt(@NotNull String data, @NotNull IKeyStorage keyStorage) throws EncryptionException,
            ReadingKeyException;

    byte[] encryptBytes(byte @NotNull [] data, @NotNull IKeyStorage keyStorage) throws EncryptionException,
            ReadingKeyException;

}