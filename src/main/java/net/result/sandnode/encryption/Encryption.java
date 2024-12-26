package net.result.sandnode.encryption;

import net.result.sandnode.encryption.interfaces.*;
import net.result.sandnode.encryption.none.NoneKeyStorage;

public enum Encryption implements IEncryption {
    NONE {
        @Override
        public byte asByte() {
            return 0;
        }

        @Override
        public NoneKeyStorage generate() {
            return new NoneKeyStorage();
        }

        @Override
        public byte[] encrypt(String data, IKeyStorage keyStorage) {
            return data.getBytes();
        }

        @Override
        public byte[] encryptBytes(byte[] bytes, IKeyStorage keyStorage) {
            return bytes;
        }

        @Override
        public String decrypt(byte[] encryptedData, IKeyStorage keyStorage) {
            return new String(encryptedData);
        }

        @Override
        public byte[] decryptBytes(byte[] encryptedBytes, IKeyStorage keyStorage) {
            return encryptedBytes;
        }

    };

    @Override
    public boolean isAsymmetric() {
        return false;
    }

    @Override
    public boolean isSymmetric() {
        return false;
    }
}
