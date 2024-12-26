package net.result.sandnode.encryption.interfaces;

import net.result.sandnode.exceptions.*;

public interface IAsymmetricEncryption extends IEncryption {
    IAsymmetricConvertor publicKeyConvertor();

    IAsymmetricConvertor privateKeyConvertor();

    @Override
    IAsymmetricKeyStorage generate();

    @Override
    byte[] encryptBytes(byte[] bytes, IKeyStorage keyStorage) throws EncryptionException, WrongKeyException,
            CannotUseEncryption;

    @Override
    byte[] decryptBytes(byte[] encryptedBytes, IKeyStorage keyStorage) throws DecryptionException, WrongKeyException,
            CannotUseEncryption, PrivateKeyNotFoundException;

    IAsymmetricKeyStorage merge(IAsymmetricKeyStorage publicKeyStorage, IAsymmetricKeyStorage privateKeyStorage);

    @Override
    default boolean isAsymmetric() {
        return true;
    }

    @Override
    default boolean isSymmetric() {
        return false;
    }
}
