package net.result.sandnode.encryption;

import net.result.sandnode.encryption.aes.*;
import net.result.sandnode.encryption.interfaces.IKeyStorage;
import net.result.sandnode.encryption.interfaces.ISymmetricEncryption;
import net.result.sandnode.exception.DecryptionException;
import net.result.sandnode.exception.EncryptionException;
import org.jetbrains.annotations.NotNull;

public enum SymmetricEncryption implements ISymmetricEncryption {
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
        public byte[] encrypt(String data, IKeyStorage keyStorage) throws EncryptionException {
            return AESEncryptor.encrypt(data, keyStorage);
        }

        @Override
        public byte[] encryptBytes(byte[] bytes, IKeyStorage keyStorage) throws EncryptionException {
            return AESEncryptor.encryptBytes(bytes, keyStorage);
        }

        @Override
        public String decrypt(byte[] encryptedData, IKeyStorage keyStorage) throws DecryptionException {
            return AESDecryptor.decrypt(encryptedData, (AESKeyStorage) keyStorage);
        }

        @Override
        public byte[] decryptBytes(byte[] encryptedBytes, IKeyStorage keyStorage) throws DecryptionException {
            return AESDecryptor.decryptBytes(encryptedBytes, (AESKeyStorage) keyStorage);
        }

        @Override
        public @NotNull AESKeyStorage toKeyStorage(byte[] body) {
            return AESKeyConvertor.toKeyStorage(body);
        }
    }
}
