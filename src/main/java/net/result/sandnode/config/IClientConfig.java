package net.result.sandnode.config;

import net.result.sandnode.encryption.interfaces.ISymmetricEncryption;
import net.result.sandnode.exception.*;
import net.result.sandnode.util.Endpoint;
import net.result.sandnode.encryption.interfaces.IAsymmetricKeyStorage;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface IClientConfig {
    @NotNull ISymmetricEncryption symmetricKeyEncryption();

    void saveKey(
            @NotNull Endpoint endpoint,
            @NotNull IAsymmetricKeyStorage keyStorage
    ) throws FSException, KeyAlreadySaved;

    Optional<IAsymmetricKeyStorage> getPublicKey(@NotNull Endpoint endpoint);
}
