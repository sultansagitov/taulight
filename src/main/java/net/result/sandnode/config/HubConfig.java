package net.result.sandnode.config;

import net.result.sandnode.encryption.interfaces.IAsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.ISymmetricEncryption;

public record HubConfig(
        IAsymmetricEncryption mainEncryption,
        ISymmetricEncryption symmetricKeyEncryption
) implements IHubConfig {}
