package net.result.sandnode.config;

import net.result.sandnode.util.Endpoint;

import java.nio.file.Path;

public record ServerConfig(
        Endpoint endpoint,
        Path publicKeyPath,
        Path privateKeyPath
) implements IServerConfig {
}
