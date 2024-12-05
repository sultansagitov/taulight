package net.result.sandnode.encryption.interfaces;

import net.result.sandnode.exceptions.CreatingKeyException;
import org.jetbrains.annotations.NotNull;

public interface IAsymmetricConvertor {

    @NotNull IAsymmetricKeyStorage toKeyStorage(@NotNull String encodedString) throws CreatingKeyException;

    @NotNull IAsymmetricKeyStorage toKeyStorage(byte @NotNull [] bytes) throws CreatingKeyException;

    @NotNull String toEncodedString(@NotNull IKeyStorage keyStorage);

}
