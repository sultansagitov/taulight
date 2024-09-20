package net.result.sandnode.util.encryption.interfaces;

import net.result.sandnode.exceptions.CreatingKeyException;
import net.result.sandnode.exceptions.ReadingKeyException;
import org.jetbrains.annotations.NotNull;

import java.security.spec.InvalidKeySpecException;

public interface IPEMConvertor {

    @NotNull IKeyStorage toKeyStorage(@NotNull String PEMString) throws CreatingKeyException;

    @NotNull IKeyStorage toKeyStorage(byte @NotNull [] bytes) throws InvalidKeySpecException, CreatingKeyException;

    @NotNull String toPEM(@NotNull IKeyStorage keyStorage) throws ReadingKeyException;

}
