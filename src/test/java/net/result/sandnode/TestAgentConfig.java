package net.result.sandnode;

import net.result.sandnode.config.AgentConfig;
import net.result.sandnode.config.KeyEntry;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.exception.error.KeyStorageNotFoundException;
import net.result.sandnode.key.Source;
import net.result.sandnode.util.Address;
import net.result.sandnode.util.Member;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class TestAgentConfig implements AgentConfig {
    @Override
    public void saveServerKey(@NotNull Source source, @NotNull Address address, @NotNull AsymmetricKeyStorage keyStorage) {}

    @Override
    public AsymmetricKeyStorage loadServerKey(@NotNull Address address) {
        throw new KeyStorageNotFoundException();
    }

    @Override
    public void savePersonalKey(@NotNull Source source, Member member, KeyStorage keyStorage) {}

    @Override
    public KeyStorage loadPersonalKey(Member member) {
        throw new KeyStorageNotFoundException(member.toString());
    }

    @Override
    public KeyStorage loadEncryptor(Member member) {
        throw new KeyStorageNotFoundException(member.toString());
    }

    @Override
    public KeyEntry loadDEK(Member m1, Member m2) {
        throw new KeyStorageNotFoundException(m1 + " - " + m2);
    }

    @Override
    public KeyStorage loadDEK(UUID keyID) {
        throw new KeyStorageNotFoundException(keyID);
    }

    @Override
    public void saveEncryptor(@NotNull Source source, Member member, KeyStorage keyStorage) {}

    @Override
    public void saveDEK(@NotNull Source source, Member m1, Member m2, UUID keyID, KeyStorage keyStorage) {}
}
