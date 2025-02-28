package net.result.sandnode.encryption.interfaces;

import net.result.sandnode.exception.crypto.CannotUseEncryption;
import net.result.sandnode.exception.crypto.CryptoException;
import net.result.sandnode.exception.error.DecryptionException;
import net.result.sandnode.exception.error.EncryptionException;
import net.result.sandnode.exception.crypto.PrivateKeyNotFoundException;
import net.result.sandnode.exception.crypto.WrongKeyException;

public interface AsymmetricEncryption extends Encryption {
    AsymmetricConvertor publicKeyConvertor();

    AsymmetricConvertor privateKeyConvertor();

    @Override
    AsymmetricKeyStorage generate();

    @Override
    byte[] encryptBytes(byte[] bytes, KeyStorage keyStorage) throws EncryptionException, CryptoException;

    @Override
    byte[] decryptBytes(byte[] encryptedBytes, KeyStorage keyStorage)
            throws DecryptionException, WrongKeyException, CannotUseEncryption, PrivateKeyNotFoundException;

    AsymmetricKeyStorage merge(AsymmetricKeyStorage publicKeyStorage, AsymmetricKeyStorage privateKeyStorage);

    @Override
    default boolean isAsymmetric() {
        return true;
    }

    @Override
    default boolean isSymmetric() {
        return false;
    }
}
