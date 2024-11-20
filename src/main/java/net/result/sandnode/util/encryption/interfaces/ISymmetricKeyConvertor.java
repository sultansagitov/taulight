package net.result.sandnode.util.encryption.interfaces;

import net.result.sandnode.exceptions.encryption.CannotUseEncryption;
import org.jetbrains.annotations.NotNull;

public interface ISymmetricKeyConvertor {
    byte @NotNull [] toBytes(ISymmetricKeyStorage keyStorage) throws CannotUseEncryption;

    @NotNull ISymmetricKeyStorage toKeyStorage(byte @NotNull [] bytes);
}
