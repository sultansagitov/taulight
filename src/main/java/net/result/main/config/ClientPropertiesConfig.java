package net.result.main.config;

import net.result.sandnode.config.ClientConfig;
import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.interfaces.SymmetricEncryption;
import net.result.sandnode.exception.ConfigurationException;
import net.result.sandnode.exception.crypto.EncryptionTypeException;
import net.result.sandnode.exception.crypto.NoSuchEncryptionException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ClientPropertiesConfig implements ClientConfig {
    private final SymmetricEncryption SYMMETRIC_ENCRYPTION;

    public ClientPropertiesConfig()
            throws ConfigurationException, NoSuchEncryptionException, EncryptionTypeException {
        this("taulight.properties");
    }

    public ClientPropertiesConfig(@NotNull String fileName)
            throws ConfigurationException, NoSuchEncryptionException, EncryptionTypeException {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            properties.load(input);
        } catch (IOException e) {
            throw new ConfigurationException("Error when reading \"%s\"".formatted(fileName), e);
        }

        String symKeyProperty = properties.getProperty("client.keys.symmetric");
        SYMMETRIC_ENCRYPTION = EncryptionManager.find(symKeyProperty).symmetric();
    }

    @Override
    public @NotNull SymmetricEncryption symmetricKeyEncryption() {
        return SYMMETRIC_ENCRYPTION;
    }
}
