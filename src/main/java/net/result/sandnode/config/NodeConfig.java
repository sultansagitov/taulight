package net.result.sandnode.config;

import net.result.sandnode.util.encryption.Encryption;
import org.jetbrains.annotations.NotNull;

public interface NodeConfig {
    @NotNull Encryption getMainEncryption();

    @NotNull Encryption getSymmetricKeyEncryption();
}
