package net.result.sandnode.encryption;

import net.result.sandnode.encryption.ecies.*;
import net.result.sandnode.encryption.interfaces.*;

public enum AsymmetricEncryptions implements AsymmetricEncryption {
    ECIES {
        @Override
        public byte asByte() {
            return 1;
        }
        @Override
        public ECIESKeyStorage generate() {
            return ECIESKeyGenerator.generate();
        }
        @Override
        public byte[] encrypt(String data, KeyStorage keyStorage) {
            return ECIESEncryptor.encrypt(data, keyStorage);
        }
        @Override
        public byte[] encryptBytes(byte[] bytes, KeyStorage keyStorage) {
            return ECIESEncryptor.encryptBytes(bytes, keyStorage);
        }
        @Override
        public String decrypt(byte[] encryptedData, KeyStorage keyStorage) {
            return ECIESDecryptor.decrypt(encryptedData, keyStorage);
        }
        @Override
        public byte[] decryptBytes(byte[] encryptedBytes, KeyStorage keyStorage) {
            return ECIESDecryptor.decryptBytes(encryptedBytes, keyStorage);
        }
        @Override
        public ECIESKeyStorage merge(AsymmetricKeyStorage publicKeyStorage, AsymmetricKeyStorage privateKeyStorage) {
            return new ECIESKeyStorage(
                ((ECIESKeyStorage) publicKeyStorage).publicKey(),
                ((ECIESKeyStorage) privateKeyStorage).privateKey()
            );
        }
        @Override
        public ECIESPublicKeyConvertor publicKeyConvertor() {
            return ECIESPublicKeyConvertor.instance();
        }
        @Override
        public ECIESPrivateKeyConvertor privateKeyConvertor() {
            return ECIESPrivateKeyConvertor.instance();
        }
    }
}
