package net.result.sandnode.util.encryption.interfaces;

import net.result.sandnode.exceptions.CreatingKeyException;
import net.result.sandnode.exceptions.ReadingKeyException;
import org.jetbrains.annotations.NotNull;

public interface IAsymmetricConvertor {

    @NotNull IAsymmetricKeyStorage toKeyStorage(@NotNull String encodedString) throws CreatingKeyException;

    @NotNull IAsymmetricKeyStorage toKeyStorage(byte @NotNull [] bytes) throws CreatingKeyException;

    @NotNull String toEncodedString(@NotNull IKeyStorage keyStorage) throws ReadingKeyException;

}
