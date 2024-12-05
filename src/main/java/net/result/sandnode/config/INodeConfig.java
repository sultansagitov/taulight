package net.result.sandnode.config;

import net.result.sandnode.encryption.interfaces.IAsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.ISymmetricEncryption;
import org.jetbrains.annotations.NotNull;

public interface INodeConfig {
    @NotNull IAsymmetricEncryption mainEncryption();

    @NotNull ISymmetricEncryption symmetricKeyEncryption();
}
