package net.result.sandnode.config;

import net.result.sandnode.encryption.interfaces.IAsymmetricEncryption;
import net.result.sandnode.util.Endpoint;
import net.result.sandnode.util.db.IDatabase;
import net.result.sandnode.util.group.GroupManager;
import net.result.sandnode.util.tokens.ITokenizer;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public interface IServerConfig {
    Endpoint endpoint();
    Path publicKeyPath();
    Path privateKeyPath();

    @NotNull IAsymmetricEncryption mainEncryption();

    GroupManager groupManager();

    IDatabase database();

    ITokenizer tokenizer();
}
