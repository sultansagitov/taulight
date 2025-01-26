package net.result.sandnode.encryption.interfaces;

import org.jetbrains.annotations.NotNull;

public interface ISymmetricEncryption extends IEncryption {

    @NotNull SymmetricKeyStorage generate();

    @NotNull SymmetricKeyStorage toKeyStorage(byte[] body);

    @Override
    default boolean isAsymmetric() {
        return false;
    }

    @Override
    default boolean isSymmetric() {
        return true;
    }
}
