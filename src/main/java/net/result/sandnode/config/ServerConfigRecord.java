package net.result.sandnode.config;

import net.result.sandnode.encryption.KeyStorageRegistry;
import net.result.sandnode.encryption.interfaces.AsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.security.PasswordHasher;
import net.result.sandnode.util.Container;
import net.result.sandnode.util.Endpoint;
import net.result.sandnode.group.GroupManager;
import net.result.sandnode.security.Tokenizer;

import java.nio.file.Path;

public record ServerConfigRecord(
        Container container,
        Endpoint endpoint,
        Path publicKeyPath,
        Path privateKeyPath,
        AsymmetricEncryption mainEncryption,
        GroupManager groupManager,
        Tokenizer tokenizer,
        PasswordHasher hasher
) implements ServerConfig {
    @Override
    public void saveKey(AsymmetricKeyStorage keyStorage) {}

    @Override
    public KeyStorageRegistry readKey(AsymmetricEncryption mainEncryption) {
        return null;
    }
}
