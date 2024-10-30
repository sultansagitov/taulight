package net.result.sandnode.util.encryption;

import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.util.encryption.aes.AESDecryptor;
import net.result.sandnode.util.encryption.aes.AESEncryptor;
import net.result.sandnode.util.encryption.aes.AESGenerator;
import net.result.sandnode.util.encryption.interfaces.IDecryptor;
import net.result.sandnode.util.encryption.interfaces.IEncryptor;
import net.result.sandnode.util.encryption.interfaces.IGenerator;
import net.result.sandnode.util.encryption.no.NoDecryptor;
import net.result.sandnode.util.encryption.no.NoEncryptor;
import net.result.sandnode.util.encryption.no.NoGenerator;
import net.result.sandnode.util.encryption.rsa.RSADecryptor;
import net.result.sandnode.util.encryption.rsa.RSAEncryptor;
import net.result.sandnode.util.encryption.rsa.RSAGenerator;
import org.jetbrains.annotations.NotNull;

public enum Encryption {

    NO((byte) 0, false, false) {
        @Override
        public IGenerator generator() {
            return NoGenerator.getInstance();
        }

        @Override
        public IEncryptor encryptor() {
            return NoEncryptor.getInstance();
        }

        @Override
        public IDecryptor decryptor() {
            return NoDecryptor.getInstance();
        }
    },

    RSA((byte) 1, true, false) {
        @Override
        public RSAGenerator generator() {
            return RSAGenerator.getInstance();
        }

        @Override
        public RSAEncryptor encryptor() {
            return RSAEncryptor.getInstance();
        }

        @Override
        public RSADecryptor decryptor() {
            return RSADecryptor.getInstance();
        }
    },

    AES((byte) 2, false, true) {
        @Override
        public AESGenerator generator() {
            return AESGenerator.getInstance();
        }

        @Override
        public AESEncryptor encryptor() {
            return AESEncryptor.getInstance();
        }

        @Override
        public AESDecryptor decryptor() {
            return AESDecryptor.getInstance();
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
            case 0 -> NO;
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
