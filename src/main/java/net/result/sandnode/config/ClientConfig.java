package net.result.sandnode.config;

import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.encryption.interfaces.SymmetricEncryption;
import net.result.sandnode.exception.FSException;
import net.result.sandnode.exception.crypto.KeyAlreadySaved;
import net.result.sandnode.exception.error.KeyStorageNotFoundException;
import net.result.sandnode.util.Endpoint;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public interface ClientConfig {
    @NotNull SymmetricEncryption symmetricKeyEncryption();

    void saveKey(@NotNull Endpoint endpoint, @NotNull AsymmetricKeyStorage keyStorage)
            throws FSException, KeyAlreadySaved;

    Optional<AsymmetricKeyStorage> getPublicKey(@NotNull Endpoint endpoint);

    // TODO explain terminology

    void savePersonalKey(UUID keyID, KeyStorage keyStorage) throws FSException; // TODO replace FSException

    void saveEncryptor(String nickname, UUID keyID, KeyStorage keyStorage) throws FSException;

    void saveDEK(String nickname, UUID keyID, KeyStorage keyStorage) throws FSException;

    KeyStorage loadPersonalKey(UUID keyID) throws KeyStorageNotFoundException;

    KeyEntry loadEncryptor(String nickname) throws KeyStorageNotFoundException;

    KeyEntry loadDEK(String nickname) throws KeyStorageNotFoundException;

    KeyStorage loadDEK(UUID keyID) throws KeyStorageNotFoundException;
}
