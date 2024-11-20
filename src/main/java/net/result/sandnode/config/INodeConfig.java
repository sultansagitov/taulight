package net.result.sandnode.config;

import net.result.sandnode.util.encryption.interfaces.IAsymmetricEncryption;
import net.result.sandnode.util.encryption.interfaces.ISymmetricEncryption;
import org.jetbrains.annotations.NotNull;

public interface INodeConfig {
    @NotNull IAsymmetricEncryption getMainEncryption();

    @NotNull ISymmetricEncryption getSymmetricKeyEncryption();
}
