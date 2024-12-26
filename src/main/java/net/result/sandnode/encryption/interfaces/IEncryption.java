package net.result.sandnode.encryption.interfaces;

import net.result.sandnode.exceptions.*;

public interface IEncryption {

    byte asByte();

    boolean isAsymmetric();

    boolean isSymmetric();

    String name();

    IKeyStorage generate();

    byte[] encrypt(String data, IKeyStorage keyStorage) throws EncryptionException, WrongKeyException, CannotUseEncryption;

    byte[] encryptBytes(byte[] bytes, IKeyStorage keyStorage) throws EncryptionException, WrongKeyException,
            CannotUseEncryption;

    String decrypt(byte[] encryptedData, IKeyStorage keyStorage) throws WrongKeyException, CannotUseEncryption,
            DecryptionException, PrivateKeyNotFoundException;

    byte[] decryptBytes(byte[] encryptedBytes, IKeyStorage keyStorage) throws DecryptionException, WrongKeyException,
            CannotUseEncryption, PrivateKeyNotFoundException;

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
