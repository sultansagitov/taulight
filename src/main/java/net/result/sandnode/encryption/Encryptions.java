package net.result.sandnode.encryption;

import net.result.sandnode.encryption.interfaces.*;
import net.result.sandnode.encryption.none.NoneKeyStorage;

public enum Encryptions implements Encryption {
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
        public byte[] encrypt(String data, KeyStorage keyStorage) {
            return data.getBytes();
        }

        @Override
        public byte[] encryptBytes(byte[] bytes, KeyStorage keyStorage) {
            return bytes;
        }

        @Override
        public String decrypt(byte[] encryptedData, KeyStorage keyStorage) {
            return new String(encryptedData);
        }

        @Override
        public byte[] decryptBytes(byte[] encryptedBytes, KeyStorage keyStorage) {
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
