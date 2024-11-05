package net.result.sandnode.util.encryption.asymmetric.interfaces;

import net.result.sandnode.exceptions.CreatingKeyException;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.util.encryption.asymmetric.AsymmetricKeyStorage;
import net.result.sandnode.util.encryption.core.interfaces.IKeyStorage;
import org.jetbrains.annotations.NotNull;

public interface IAsymmetricConvertor {

    @NotNull AsymmetricKeyStorage toKeyStorage(@NotNull String encodedString) throws CreatingKeyException;

    @NotNull AsymmetricKeyStorage toKeyStorage(byte @NotNull [] bytes) throws CreatingKeyException;

    @NotNull String toEncodedString(@NotNull IKeyStorage keyStorage) throws ReadingKeyException;

}
