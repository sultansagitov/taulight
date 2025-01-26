package net.result.sandnode.encryption.interfaces;

import net.result.sandnode.exception.CannotUseEncryption;
import net.result.sandnode.exception.CreatingKeyException;
import org.jetbrains.annotations.NotNull;

public interface AsymmetricConvertor {
    @NotNull AsymmetricKeyStorage toKeyStorage(@NotNull String encodedString) throws CreatingKeyException;

    @NotNull String toEncodedString(@NotNull KeyStorage keyStorage) throws CannotUseEncryption;
}
