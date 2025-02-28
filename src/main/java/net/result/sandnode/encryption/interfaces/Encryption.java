package net.result.sandnode.encryption.interfaces;

import net.result.sandnode.exception.crypto.*;
import net.result.sandnode.exception.error.*;

public interface Encryption {

    byte asByte();

    boolean isAsymmetric();

    boolean isSymmetric();

    String name();

    KeyStorage generate();

    byte[] encrypt(String data, KeyStorage keyStorage) throws EncryptionException, CryptoException;

    byte[] encryptBytes(byte[] bytes, KeyStorage keyStorage) throws EncryptionException, CryptoException;

    String decrypt(byte[] encryptedData, KeyStorage keyStorage)
            throws WrongKeyException, CannotUseEncryption, DecryptionException, PrivateKeyNotFoundException;

    byte[] decryptBytes(byte[] encryptedBytes, KeyStorage keyStorage)
            throws DecryptionException, WrongKeyException, CannotUseEncryption, PrivateKeyNotFoundException;

    default AsymmetricEncryption asymmetric() throws EncryptionTypeException {
        if (isAsymmetric())
            return (AsymmetricEncryption) this;
        throw new EncryptionTypeException(this);
    }

    default SymmetricEncryption symmetric() throws EncryptionTypeException {
        if (isSymmetric())
            return (SymmetricEncryption) this;
        throw new EncryptionTypeException(this);
    }
}
