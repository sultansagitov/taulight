package net.result.sandnode.util.encryption.asymmetric.interfaces;

import net.result.sandnode.config.HubConfig;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.util.encryption.core.interfaces.IKeyStorage;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

public interface IAsymmetricKeySaver {

    void savePublicKey(@NotNull Path publicKeyPath, @NotNull IKeyStorage rsaKeyStorage) throws IOException,
            ReadingKeyException;

    void saveHubKeys(@NotNull HubConfig hubConfig, @NotNull IKeyStorage keyStorage) throws IOException,
            ReadingKeyException;

}
