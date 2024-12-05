package net.result.sandnode.encryption.interfaces;

import org.jetbrains.annotations.NotNull;

public interface ISymmetricKeyConvertor {
    byte @NotNull [] toBytes(ISymmetricKeyStorage keyStorage);

    @NotNull ISymmetricKeyStorage toKeyStorage(byte @NotNull [] bytes);
}
