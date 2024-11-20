package net.result.sandnode.config;

import java.nio.file.Path;

public interface IHubConfig extends INodeConfig {
    Path getKeysDir();

    Path getPublicKeyPath();

    Path getPrivateKeyPath();
}
