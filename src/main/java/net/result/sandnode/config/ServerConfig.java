package net.result.sandnode.config;

import net.result.sandnode.encryption.interfaces.AsymmetricEncryption;
import net.result.sandnode.util.Endpoint;
import net.result.sandnode.db.Database;
import net.result.sandnode.group.GroupManager;
import net.result.sandnode.security.Tokenizer;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public interface ServerConfig {
    Endpoint endpoint();
    Path publicKeyPath();
    Path privateKeyPath();

    @NotNull AsymmetricEncryption mainEncryption();

    GroupManager groupManager();

    Database database();

    Tokenizer tokenizer();
}
