package net.result.sandnode.util.encryption.asymmetric.rsa;

import net.result.sandnode.exceptions.CreatingKeyException;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.util.encryption.asymmetric.interfaces.IAsymmetricConvertor;
import net.result.sandnode.util.encryption.interfaces.IKeyStorage;
import org.jetbrains.annotations.NotNull;

import java.security.spec.InvalidKeySpecException;

public interface IRSAConvertor extends IAsymmetricConvertor {
    @Override
    @NotNull RSAKeyStorage toKeyStorage(@NotNull String PEMString) throws CreatingKeyException;

    @Override
    @NotNull RSAKeyStorage toKeyStorage(byte @NotNull [] bytes) throws InvalidKeySpecException, CreatingKeyException;

    @Override
    @NotNull String toPEM(@NotNull IKeyStorage keyStorage) throws ReadingKeyException;
}
