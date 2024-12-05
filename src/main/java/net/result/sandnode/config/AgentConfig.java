package net.result.sandnode.config;

import net.result.sandnode.encryption.interfaces.IAsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.ISymmetricEncryption;

public record AgentConfig(
        IAsymmetricEncryption mainEncryption,
        ISymmetricEncryption symmetricKeyEncryption
) implements IAgentConfig {}
