package net.result.sandnode.util.encryption;

import net.result.sandnode.util.encryption.interfaces.IAsymmetricConvertor;
import net.result.sandnode.util.encryption.interfaces.IAsymmetricEncryption;
import net.result.sandnode.util.encryption.interfaces.IAsymmetricKeyReader;
import net.result.sandnode.util.encryption.interfaces.IAsymmetricKeySaver;
import net.result.sandnode.util.encryption.rsa.*;

public enum AsymmetricEncryption implements IAsymmetricEncryption {
    RSA {
        @Override
        public RSAGenerator generator() {
            return RSAGenerator.instance();
        }

        @Override
        public RSAEncryptor encryptor() {
            return RSAEncryptor.instance();
        }

        @Override
        public RSADecryptor decryptor() {
            return RSADecryptor.instance();
        }

        @Override
        public byte asByte() {
            return 1;
        }

        @Override
        public boolean isAsymmetric() {
            return true;
        }

        @Override
        public boolean isSymmetric() {
            return false;
        }

        @Override
        public IAsymmetricConvertor publicKeyConvertor() {
            return RSAPublicKeyConvertor.instance();
        }

        @Override
        public IAsymmetricConvertor privateKeyConvertor() {
            return RSAPrivateKeyConvertor.instance();
        }

        @Override
        public IAsymmetricKeySaver keySaver() {
            return RSAKeySaver.instance();
        }

        @Override
        public IAsymmetricKeyReader keyReader() {
            return RSAKeyReader.instance();
        }
    }
}
