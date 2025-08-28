package net.result.sandnode;

import net.result.sandnode.config.AgentConfig;
import net.result.sandnode.config.KeyEntry;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.exception.error.KeyStorageNotFoundException;
import net.result.sandnode.util.Address;
import net.result.sandnode.util.Member;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class TestAgentConfig implements AgentConfig {
    @Override
    public void saveServerKey(@NotNull Address address, @NotNull AsymmetricKeyStorage keyStorage) {}

    @Override
    public AsymmetricKeyStorage loadServerKey(@NotNull Address address) {
        throw new KeyStorageNotFoundException();
    }

    @Override
    public void savePersonalKey(Member member, KeyStorage keyStorage) {}

    @Override
    public KeyStorage loadPersonalKey(Member member) {
        throw new KeyStorageNotFoundException(member.toString());
    }

    @Override
    public KeyStorage loadEncryptor(Member member) {
        throw new KeyStorageNotFoundException(member.toString());
    }

    @Override
    public KeyEntry loadDEK(Member member) {
        throw new KeyStorageNotFoundException(member.toString());
    }

    @Override
    public KeyStorage loadDEK(Address address, UUID keyID) {
        throw new KeyStorageNotFoundException(keyID);
    }

    @Override
    public void saveEncryptor(Member member, KeyStorage keyStorage) {}

    @Override
    public void saveDEK(Member member, UUID keyID, KeyStorage keyStorage) {}
}
