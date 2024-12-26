package net.result.sandnode.encryption.interfaces;

import net.result.sandnode.exceptions.CannotUseEncryption;
import net.result.sandnode.exceptions.CreatingKeyException;
import org.jetbrains.annotations.NotNull;

public interface IAsymmetricConvertor {
    @NotNull IAsymmetricKeyStorage toKeyStorage(@NotNull String encodedString) throws CreatingKeyException;

    @NotNull String toEncodedString(@NotNull IKeyStorage keyStorage) throws CannotUseEncryption;
}
