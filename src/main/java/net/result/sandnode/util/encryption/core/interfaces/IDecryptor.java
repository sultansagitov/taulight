package net.result.sandnode.util.encryption.core.interfaces;

import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.DecryptionException;
import org.jetbrains.annotations.NotNull;

public interface IDecryptor {

    String decrypt(byte @NotNull [] data, IKeyStorage ks) throws DecryptionException, ReadingKeyException;

    byte[] decryptBytes(byte @NotNull [] data, IKeyStorage ks) throws DecryptionException, ReadingKeyException;

}