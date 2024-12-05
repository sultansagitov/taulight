package net.result.sandnode.encryption.interfaces;

import net.result.sandnode.config.IServerConfig;
import net.result.sandnode.exceptions.FSException;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public interface IAsymmetricKeySaver {

    void savePublicKey(@NotNull Path publicKeyPath, @NotNull IKeyStorage rsaKeyStorage) throws FSException;

    void saveServerKeys(@NotNull IServerConfig serverConfig, @NotNull IKeyStorage keyStorage) throws FSException;

}
