package net.result.sandnode.config;

import net.result.sandnode.encryption.interfaces.IAsymmetricEncryption;
import net.result.sandnode.util.Endpoint;
import net.result.sandnode.db.IDatabase;
import net.result.sandnode.group.GroupManager;
import net.result.sandnode.tokens.ITokenizer;

import java.nio.file.Path;

public record ServerConfig(
        Endpoint endpoint,
        Path publicKeyPath,
        Path privateKeyPath,
        IAsymmetricEncryption mainEncryption,
        GroupManager groupManager,
        IDatabase database,
        ITokenizer tokenizer
) implements IServerConfig {
}
