package net.result.sandnode.encryption.interfaces;

import net.result.sandnode.exception.crypto.CannotUseEncryption;
import net.result.sandnode.exception.crypto.CreatingKeyException;
import org.jetbrains.annotations.NotNull;

public interface AsymmetricConvertor {
    @NotNull AsymmetricKeyStorage toKeyStorage(@NotNull String encodedString) throws CreatingKeyException;

    @NotNull String toEncodedString(@NotNull KeyStorage keyStorage) throws CannotUseEncryption;
}
