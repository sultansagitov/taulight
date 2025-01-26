package net.result.sandnode.encryption.interfaces;

import net.result.sandnode.exception.*;

public interface AsymmetricEncryption extends Encryption {
    AsymmetricConvertor publicKeyConvertor();

    AsymmetricConvertor privateKeyConvertor();

    @Override
    AsymmetricKeyStorage generate();

    @Override
    byte[] encryptBytes(byte[] bytes, KeyStorage keyStorage) throws EncryptionException, CannotUseEncryption;

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
