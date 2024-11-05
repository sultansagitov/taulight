package net.result.sandnode.util.encryption;

import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.util.encryption.core.aes.AESDecryptor;
import net.result.sandnode.util.encryption.core.aes.AESEncryptor;
import net.result.sandnode.util.encryption.core.aes.AESGenerator;
import net.result.sandnode.util.encryption.core.interfaces.IDecryptor;
import net.result.sandnode.util.encryption.core.interfaces.IEncryptor;
import net.result.sandnode.util.encryption.core.interfaces.IGenerator;
import net.result.sandnode.util.encryption.core.none.NoneDecryptor;
import net.result.sandnode.util.encryption.core.none.NoneEncryptor;
import net.result.sandnode.util.encryption.core.none.NoneGenerator;
import net.result.sandnode.util.encryption.core.rsa.RSADecryptor;
import net.result.sandnode.util.encryption.core.rsa.RSAEncryptor;
import net.result.sandnode.util.encryption.core.rsa.RSAGenerator;
import org.jetbrains.annotations.NotNull;

public enum Encryption {

    NONE((byte) 0, false, false) {
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
    },

    RSA((byte) 1, true, false) {
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
    },

    AES((byte) 2, false, true) {
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
    };

    public final byte encryption;
    public final boolean isAsymmetric;
    public final boolean isSymmetric;

    Encryption(byte encryption, boolean isAsymmetric, boolean isSymmetric) {
        this.encryption = encryption;
        this.isAsymmetric = isAsymmetric;
        this.isSymmetric = isSymmetric;
    }

    public static @NotNull Encryption fromByte(byte encryption) throws NoSuchEncryptionException {
        return switch (encryption) {
            case 0 -> NONE;
            case 1 -> RSA;
            case 2 -> AES;
            default -> throw new NoSuchEncryptionException(encryption);
        };
    }

    public abstract IGenerator generator();

    public abstract IEncryptor encryptor();

    public abstract IDecryptor decryptor();

    public byte asByte() {
        return encryption;
    }

}
