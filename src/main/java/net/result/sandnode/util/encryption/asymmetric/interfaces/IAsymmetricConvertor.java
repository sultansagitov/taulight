package net.result.sandnode.util.encryption.asymmetric.interfaces;

import net.result.sandnode.exceptions.CreatingKeyException;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.util.encryption.asymmetric.AsymmetricKeyStorage;
import net.result.sandnode.util.encryption.interfaces.IKeyStorage;
import net.result.sandnode.util.encryption.interfaces.IPEMConvertor;
import org.jetbrains.annotations.NotNull;

import java.security.spec.InvalidKeySpecException;

public interface IAsymmetricConvertor extends IPEMConvertor {
    @Override
    @NotNull AsymmetricKeyStorage toKeyStorage(@NotNull String PEMString) throws CreatingKeyException;

    @Override
    @NotNull AsymmetricKeyStorage toKeyStorage(byte @NotNull [] bytes) throws InvalidKeySpecException, CreatingKeyException;

    @Override
    @NotNull String toPEM(@NotNull IKeyStorage keyStorage) throws ReadingKeyException;
}
