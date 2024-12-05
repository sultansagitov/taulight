package net.result.sandnode.encryption.interfaces;

import net.result.sandnode.exceptions.EncryptionException;
import org.jetbrains.annotations.NotNull;

public interface IEncryptor {

    byte[] encrypt(@NotNull String data, @NotNull IKeyStorage keyStorage) throws EncryptionException;

    byte[] encryptBytes(byte @NotNull [] data, @NotNull IKeyStorage keyStorage) throws EncryptionException;

}