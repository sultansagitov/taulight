package net.result.sandnode.encryption.interfaces;

import net.result.sandnode.exception.crypto.*;

public interface Encryption {

    byte asByte();

    boolean isAsymmetric();

    boolean isSymmetric();

    String name();

    KeyStorage generate();

    byte[] encrypt(String data, KeyStorage keyStorage);

    byte[] encryptBytes(byte[] bytes, KeyStorage keyStorage);

    String decrypt(byte[] encryptedData, KeyStorage keyStorage);

    byte[] decryptBytes(byte[] encryptedBytes, KeyStorage keyStorage);

    default AsymmetricEncryption asymmetric() {
        if (isAsymmetric())
            return (AsymmetricEncryption) this;
        throw new EncryptionTypeException(this);
    }

    default SymmetricEncryption symmetric() {
        if (isSymmetric())
            return (SymmetricEncryption) this;
        throw new EncryptionTypeException(this);
    }
}
