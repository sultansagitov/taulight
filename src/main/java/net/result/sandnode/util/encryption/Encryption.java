package net.result.sandnode.util.encryption;

import net.result.sandnode.util.encryption.interfaces.IDecryptor;
import net.result.sandnode.util.encryption.interfaces.IEncryption;
import net.result.sandnode.util.encryption.interfaces.IEncryptor;
import net.result.sandnode.util.encryption.interfaces.IGenerator;
import net.result.sandnode.util.encryption.none.NoneDecryptor;
import net.result.sandnode.util.encryption.none.NoneEncryptor;
import net.result.sandnode.util.encryption.none.NoneGenerator;

public enum Encryption implements IEncryption {

    NONE {
        @Override
        public IGenerator generator() {
            return NoneGenerator.instance();
        }

        @Override
        public IEncryptor encryptor() {
            return NoneEncryptor.instance();
        }

        @Override
        public IDecryptor decryptor() {
            return NoneDecryptor.instance();
        }

        @Override
        public byte asByte() {
            return 0;
        }

        @Override
        public boolean isAsymmetric() {
            return false;
        }

        @Override
        public boolean isSymmetric() {
            return false;
        }
    }
}
