package net.result.sandnode.util.encryption.interfaces;

public interface IEncryption {
    IGenerator generator();

    IEncryptor encryptor();

    IDecryptor decryptor();

    byte asByte();

    boolean isAsymmetric();

    boolean isSymmetric();

    String name();
}
