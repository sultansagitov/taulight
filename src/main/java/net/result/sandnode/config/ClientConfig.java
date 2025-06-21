package net.result.sandnode.config;

import net.result.sandnode.encryption.interfaces.SymmetricEncryption;
import org.jetbrains.annotations.NotNull;

public interface ClientConfig {
    @NotNull SymmetricEncryption symmetricKeyEncryption();
}
