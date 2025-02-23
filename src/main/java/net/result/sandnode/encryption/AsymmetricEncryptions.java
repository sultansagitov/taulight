package net.result.sandnode.encryption;

import net.result.sandnode.encryption.ecies.*;
import net.result.sandnode.encryption.interfaces.*;

import net.result.sandnode.exception.CannotUseEncryption;
import net.result.sandnode.exception.DecryptionException;
import net.result.sandnode.exception.EncryptionException;
import net.result.sandnode.exception.PrivateKeyNotFoundException;
import net.result.sandnode.exception.WrongKeyException;

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
        public byte[] encrypt(String data, KeyStorage keyStorage) throws EncryptionException, CannotUseEncryption {
            return ECIESEncryptor.encrypt(data, keyStorage);
        }
        @Override
        public byte[] encryptBytes(byte[] bytes, KeyStorage keyStorage)
                throws EncryptionException, CannotUseEncryption {
            return ECIESEncryptor.encryptBytes(bytes, keyStorage);
        }
        @Override
        public String decrypt(byte[] encryptedData, KeyStorage keyStorage)
                throws WrongKeyException, CannotUseEncryption, DecryptionException, PrivateKeyNotFoundException {
            return ECIESDecryptor.decrypt(encryptedData, keyStorage);
        }
        @Override
        public byte[] decryptBytes(byte[] encryptedBytes, KeyStorage keyStorage)
                throws DecryptionException, WrongKeyException, CannotUseEncryption, PrivateKeyNotFoundException {
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
