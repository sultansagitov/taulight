package net.result.sandnode.util.encryption;

import net.result.sandnode.util.encryption.aes.AESDecryptor;
import net.result.sandnode.util.encryption.aes.AESEncryptor;
import net.result.sandnode.util.encryption.aes.AESGenerator;
import net.result.sandnode.util.encryption.aes.AESKeyConvertor;
import net.result.sandnode.util.encryption.interfaces.ISymmetricEncryption;
import net.result.sandnode.util.encryption.interfaces.ISymmetricKeyConvertor;

public enum SymmetricEncryption implements ISymmetricEncryption {
    AES {
        @Override
        public AESGenerator generator() {
            return AESGenerator.instance();
        }

        @Override
        public AESEncryptor encryptor() {
            return AESEncryptor.instance();
        }

        @Override
        public AESDecryptor decryptor() {
            return AESDecryptor.instance();
        }

        @Override
        public byte asByte() {
            return 2;
        }

        @Override
        public boolean isAsymmetric() {
            return false;
        }

        @Override
        public boolean isSymmetric() {
            return true;
        }

        @Override
        public ISymmetricKeyConvertor keyConvertor() {
            return AESKeyConvertor.instance();
        }
    }
}
