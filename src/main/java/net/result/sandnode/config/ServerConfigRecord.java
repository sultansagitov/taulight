package net.result.sandnode.config;

import net.result.sandnode.encryption.KeyStorageRegistry;
import net.result.sandnode.encryption.interfaces.AsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.util.Container;
import net.result.sandnode.util.Address;

public record ServerConfigRecord(
        Container container,
        Address address,
        AsymmetricEncryption mainEncryption
) implements ServerConfig {
    @Override
    public void saveKey(AsymmetricKeyStorage keyStorage) {}

    @Override
    public KeyStorageRegistry readKey(AsymmetricEncryption mainEncryption) {
        return null;
    }
}
