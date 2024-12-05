package net.result.sandnode.config;

import net.result.sandnode.exceptions.CreatingKeyException;
import net.result.sandnode.exceptions.CannotUseEncryption;
import net.result.sandnode.exceptions.NoSuchEncryptionException;
import net.result.sandnode.exceptions.FSException;
import net.result.sandnode.util.Endpoint;
import net.result.sandnode.encryption.interfaces.IAsymmetricKeyStorage;
import net.result.sandnode.encryption.interfaces.IKeyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IClientConfig {
    void addKey(@NotNull Endpoint endpoint, @NotNull IKeyStorage keyStorage) throws FSException;

    void saveKeysJSON() throws FSException;

    @Nullable IAsymmetricKeyStorage getPublicKey(@NotNull Endpoint endpoint) throws NoSuchEncryptionException,
            CreatingKeyException, CannotUseEncryption, FSException;
}
