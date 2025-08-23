package net.result.sandnode.encryption.interfaces;

public interface AsymmetricEncryption extends Encryption {
    AsymmetricConvertor publicKeyConvertor();

    AsymmetricConvertor privateKeyConvertor();

    @Override
    AsymmetricKeyStorage generate();

    @Override
    byte[] encryptBytes(byte[] bytes, KeyStorage keyStorage);

    @Override
    byte[] decryptBytes(byte[] encryptedBytes, KeyStorage keyStorage);

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
