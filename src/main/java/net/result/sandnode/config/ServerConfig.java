package net.result.sandnode.config;

import net.result.sandnode.encryption.interfaces.IAsymmetricEncryption;
import net.result.sandnode.util.Endpoint;
import net.result.sandnode.util.db.IDatabase;
import net.result.sandnode.util.group.IGroupManager;
import net.result.sandnode.util.tokens.ITokenizer;

import java.nio.file.Path;

public record ServerConfig(
        Endpoint endpoint,
        Path publicKeyPath,
        Path privateKeyPath,
        IAsymmetricEncryption mainEncryption,
        IGroupManager groupManager,
        IDatabase database,
        ITokenizer tokenizer
) implements IServerConfig {
}
