package net.result.sandnode.encryption;

import net.result.sandnode.encryption.aes.*;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.encryption.interfaces.SymmetricEncryption;
import org.jetbrains.annotations.NotNull;

public enum SymmetricEncryptions implements SymmetricEncryption {
    AES {
        @Override
        public byte asByte() {
            return 2;
        }

        @Override
        public @NotNull AESKeyStorage generate() {
            return AESGenerator.generate();
        }

        @Override
        public byte[] encrypt(String data, KeyStorage keyStorage) {
            return AESEncryptor.encrypt(data, keyStorage);
        }

        @Override
        public byte[] encryptBytes(byte[] bytes, KeyStorage keyStorage) {
            return AESEncryptor.encryptBytes(bytes, keyStorage);
        }

        @Override
        public String decrypt(byte[] encryptedData, KeyStorage keyStorage) {
            return AESDecryptor.decrypt(encryptedData, (AESKeyStorage) keyStorage);
        }

        @Override
        public byte[] decryptBytes(byte[] encryptedBytes, KeyStorage keyStorage) {
            return AESDecryptor.decryptBytes(encryptedBytes, (AESKeyStorage) keyStorage);
        }

        @Override
        public @NotNull AESKeyStorage toKeyStorage(byte[] body) {
            return AESKeyConvertor.toKeyStorage(body);
        }
    }
}
