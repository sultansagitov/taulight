package net.result.sandnode.config;

import net.result.sandnode.encryption.interfaces.IAsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.ISymmetricEncryption;
import net.result.sandnode.exceptions.ConfigurationException;
import net.result.sandnode.exceptions.CannotUseEncryption;
import net.result.sandnode.exceptions.NoSuchEncryptionException;
import net.result.sandnode.encryption.Encryptions;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.Properties;

public class AgentPropertiesConfig implements IAgentConfig {
    private final IAsymmetricEncryption MAIN_ENCRYPTION;
    private final ISymmetricEncryption SYMMETRIC_ENCRYPTION;

    public AgentPropertiesConfig() throws NoSuchEncryptionException, ConfigurationException, CannotUseEncryption {
        this("agent.properties");
    }

    public AgentPropertiesConfig(@NotNull String fileName) throws ConfigurationException, NoSuchEncryptionException,
            CannotUseEncryption {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            properties.load(input);
        } catch (Exception e) {
            throw new ConfigurationException(String.format("Error when reading \"%s\"", fileName), e);
        }

        MAIN_ENCRYPTION = Encryptions.findAsymmetric(properties.getProperty("keys.main", "RSA").toUpperCase());
        SYMMETRIC_ENCRYPTION = Encryptions.findSymmetric(properties.getProperty("keys.symmetric", "AES").toUpperCase());
    }

    @Override
    public @NotNull IAsymmetricEncryption mainEncryption() {
        return MAIN_ENCRYPTION;
    }

    @Override
    public @NotNull ISymmetricEncryption symmetricKeyEncryption() {
        return SYMMETRIC_ENCRYPTION;
    }

}
