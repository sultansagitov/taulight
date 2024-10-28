package net.result.sandnode.util.encryption.symmetric.interfaces;

import org.jetbrains.annotations.NotNull;

public interface ISymmetricKeyConvertor {
    @NotNull SymmetricKeyStorage toKeyStorage(byte @NotNull [] bytes);
}
