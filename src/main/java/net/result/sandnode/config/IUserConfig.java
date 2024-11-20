package net.result.sandnode.config;

import net.result.sandnode.exceptions.CreatingKeyException;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.CannotUseEncryption;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.util.Endpoint;
import net.result.sandnode.util.encryption.interfaces.IAsymmetricKeyStorage;
import net.result.sandnode.util.encryption.interfaces.IKeyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public interface IUserConfig extends INodeConfig {
    void addKey(@NotNull Endpoint endpoint, @NotNull IKeyStorage keyStorage) throws ReadingKeyException,
            IOException;

    void saveKeysJSON() throws IOException;

    @Nullable IAsymmetricKeyStorage getPublicKey(@NotNull Endpoint endpoint) throws IOException,
            NoSuchEncryptionException, CreatingKeyException, CannotUseEncryption;
}
