package net.result.sandnode;

import net.result.sandnode.config.AgentConfig;
import net.result.sandnode.config.KeyEntry;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.exception.error.KeyStorageNotFoundException;
import net.result.sandnode.util.Address;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class TestAgentConfig implements AgentConfig {
    @Override
    public void saveServerKey(@NotNull Address address, @NotNull AsymmetricKeyStorage keyStorage) {
    }

    @Override
    public AsymmetricKeyStorage loadServerKey(@NotNull Address address) throws KeyStorageNotFoundException {
        throw new KeyStorageNotFoundException();
    }

    @Override
    public void savePersonalKey(Address address, String nickname, UUID keyID, KeyStorage keyStorage) {
    }

    @Override
    public KeyStorage loadPersonalKey(Address address, UUID keyID) throws KeyStorageNotFoundException {
        throw new KeyStorageNotFoundException(keyID);
    }

    @Override
    public KeyEntry loadEncryptor(Address address, String nickname) throws KeyStorageNotFoundException {
        throw new KeyStorageNotFoundException(nickname);
    }

    @Override
    public KeyEntry loadDEK(Address address, String nickname) throws KeyStorageNotFoundException {
        throw new KeyStorageNotFoundException(nickname);
    }

    @Override
    public KeyStorage loadDEK(Address address, UUID keyID) throws KeyStorageNotFoundException {
        throw new KeyStorageNotFoundException(keyID);
    }

    @Override
    public void saveEncryptor(Address address, String nickname, UUID keyID, KeyStorage keyStorage) {
    }

    @Override
    public void saveDEK(Address address, String nickname, UUID keyID, KeyStorage keyStorage) {
    }
}
