package net.result.sandnode.config;

import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.exception.StorageException;
import net.result.sandnode.exception.crypto.KeyAlreadySaved;
import net.result.sandnode.exception.error.KeyStorageNotFoundException;
import net.result.sandnode.util.Endpoint;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface AgentConfig {

    void saveKey(@NotNull Endpoint endpoint, @NotNull AsymmetricKeyStorage keyStorage)
            throws KeyAlreadySaved, StorageException;

    AsymmetricKeyStorage getPublicKey(@NotNull Endpoint endpoint) throws KeyStorageNotFoundException;

    // TODO explain terminology

    void savePersonalKey(UUID keyID, KeyStorage keyStorage) throws StorageException;

    void saveEncryptor(String nickname, UUID keyID, KeyStorage keyStorage) throws StorageException;

    void saveDEK(String nickname, UUID keyID, KeyStorage keyStorage) throws StorageException;

    KeyStorage loadPersonalKey(UUID keyID) throws KeyStorageNotFoundException;

    KeyEntry loadEncryptor(String nickname) throws KeyStorageNotFoundException;

    KeyEntry loadDEK(String nickname) throws KeyStorageNotFoundException;

    KeyStorage loadDEK(UUID keyID) throws KeyStorageNotFoundException;
}
