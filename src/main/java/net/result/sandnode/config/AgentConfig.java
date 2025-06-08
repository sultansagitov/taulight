package net.result.sandnode.config;

import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.exception.StorageException;
import net.result.sandnode.exception.crypto.KeyAlreadySaved;
import net.result.sandnode.exception.error.KeyStorageNotFoundException;
import net.result.sandnode.util.Address;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface AgentConfig {

    void saveKey(@NotNull Address address, @NotNull AsymmetricKeyStorage keyStorage)
            throws KeyAlreadySaved, StorageException;

    AsymmetricKeyStorage getPublicKey(@NotNull Address address) throws KeyStorageNotFoundException;

    // TODO explain terminology

    void savePersonalKey(Address address, UUID keyID, KeyStorage keyStorage) throws StorageException;

    void saveEncryptor(Address address, String nickname, UUID keyID, KeyStorage keyStorage) throws StorageException;

    void saveDEK(Address address, String nickname, UUID keyID, KeyStorage keyStorage) throws StorageException;

    KeyStorage loadPersonalKey(Address address, UUID keyID) throws KeyStorageNotFoundException;

    KeyEntry loadEncryptor(Address address, String nickname) throws KeyStorageNotFoundException;

    KeyEntry loadDEK(Address address, String nickname) throws KeyStorageNotFoundException;

    KeyStorage loadDEK(Address address, UUID keyID) throws KeyStorageNotFoundException;
}
