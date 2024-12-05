package net.result.sandnode.encryption.interfaces;

import net.result.sandnode.exceptions.DecryptionException;
import org.jetbrains.annotations.NotNull;

public interface IDecryptor {

    String decrypt(byte @NotNull [] data, IKeyStorage ks) throws DecryptionException;

    byte[] decryptBytes(byte @NotNull [] data, IKeyStorage ks) throws DecryptionException;

}