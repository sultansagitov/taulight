package net.result.sandnode.config;

import net.result.sandnode.encryption.KeyStorageRegistry;
import net.result.sandnode.encryption.interfaces.AsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.exception.crypto.SavingKeyException;
import net.result.sandnode.exception.crypto.CreatingKeyException;
import net.result.sandnode.exception.crypto.ReadingKeyException;
import net.result.sandnode.security.PasswordHasher;
import net.result.sandnode.util.Container;
import net.result.sandnode.util.Endpoint;
import net.result.sandnode.group.GroupManager;
import net.result.sandnode.security.Tokenizer;
import org.jetbrains.annotations.NotNull;

public interface ServerConfig {
    Container container();

    Endpoint endpoint();

    @NotNull AsymmetricEncryption mainEncryption();

    GroupManager groupManager();

    Tokenizer tokenizer();

    void saveKey(AsymmetricKeyStorage keyStorage) throws SavingKeyException;

    KeyStorageRegistry readKey(AsymmetricEncryption mainEncryption) throws CreatingKeyException, ReadingKeyException;

    PasswordHasher hasher();
}
