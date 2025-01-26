package net.result.sandnode.encryption.interfaces;

import net.result.sandnode.exception.*;

public interface IEncryption {

    byte asByte();

    boolean isAsymmetric();

    boolean isSymmetric();

    String name();

    KeyStorage generate();

    byte[] encrypt(String data, KeyStorage keyStorage) throws EncryptionException, CannotUseEncryption;

    byte[] encryptBytes(byte[] bytes, KeyStorage keyStorage) throws EncryptionException, CannotUseEncryption;

    String decrypt(byte[] encryptedData, KeyStorage keyStorage)
            throws WrongKeyException, CannotUseEncryption, DecryptionException, PrivateKeyNotFoundException;

    byte[] decryptBytes(byte[] encryptedBytes, KeyStorage keyStorage)
            throws DecryptionException, WrongKeyException, CannotUseEncryption, PrivateKeyNotFoundException;

    default IAsymmetricEncryption asymmetric() throws EncryptionTypeException {
        if (isAsymmetric())
            return (IAsymmetricEncryption) this;
        throw new EncryptionTypeException(this);
    }

    default ISymmetricEncryption symmetric() throws EncryptionTypeException {
        if (isSymmetric())
            return (ISymmetricEncryption) this;
        throw new EncryptionTypeException(this);
    }
}
