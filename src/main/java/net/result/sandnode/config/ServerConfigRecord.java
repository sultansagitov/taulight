package net.result.sandnode.config;

import net.result.sandnode.encryption.interfaces.AsymmetricEncryption;
import net.result.sandnode.util.Endpoint;
import net.result.sandnode.db.Database;
import net.result.sandnode.group.GroupManager;
import net.result.sandnode.tokens.Tokenizer;

import java.nio.file.Path;

public record ServerConfigRecord(
        Endpoint endpoint,
        Path publicKeyPath,
        Path privateKeyPath,
        AsymmetricEncryption mainEncryption,
        GroupManager groupManager,
        Database database,
        Tokenizer tokenizer
) implements ServerConfig {
}
