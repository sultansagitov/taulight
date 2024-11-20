package net.result.sandnode.util.encryption.interfaces;

import net.result.sandnode.config.IHubConfig;
import net.result.sandnode.exceptions.ReadingKeyException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

public interface IAsymmetricKeySaver {

    void savePublicKey(@NotNull Path publicKeyPath, @NotNull IKeyStorage rsaKeyStorage) throws IOException,
            ReadingKeyException;

    void saveHubKeys(@NotNull IHubConfig hubConfig, @NotNull IKeyStorage keyStorage) throws IOException,
            ReadingKeyException;

}
