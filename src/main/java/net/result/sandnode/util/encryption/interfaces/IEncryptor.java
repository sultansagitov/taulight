package net.result.sandnode.util.encryption.interfaces;

import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.EncryptionException;

public interface IEncryptor {

    byte[] encrypt(String data, IKeyStorage keyStorage) throws EncryptionException, ReadingKeyException;

    byte[] encryptBytes(byte[] data, IKeyStorage keyStorage) throws EncryptionException, ReadingKeyException;

}