package net.result.sandnode.config;

import net.result.sandnode.exceptions.ConfigurationException;
import net.result.sandnode.exceptions.CannotUseEncryption;
import net.result.sandnode.exceptions.NoSuchEncryptionException;
import net.result.sandnode.exceptions.FSException;
import net.result.sandnode.util.FileUtil;
import net.result.sandnode.encryption.Encryptions;
import net.result.sandnode.encryption.interfaces.IAsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.ISymmetricEncryption;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class HubPropertiesConfig implements IHubConfig {
    private final IAsymmetricEncryption MAIN_ENCRYPTION;
    private final ISymmetricEncryption SYMMETRIC_ENCRYPTION;

    public HubPropertiesConfig() throws NoSuchEncryptionException, ConfigurationException, CannotUseEncryption, FSException {
        this("hub.properties");
    }

    public HubPropertiesConfig(@NotNull String fileName) throws ConfigurationException, NoSuchEncryptionException,
            CannotUseEncryption, FSException {
        Properties properties = new Properties();

        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                throw new RuntimeException(String.format("Unable to find %s", fileName));
            }
            properties.load(input);
        } catch (IOException e) {
            throw new ConfigurationException("Failed to load configuration file", fileName);
        }

        Path CONF_DIR = FileUtil.resolveHomeInPath(Paths.get(properties.getProperty("hub.dir_path")));

        FileUtil.createDir(CONF_DIR);


        MAIN_ENCRYPTION = Encryptions.findAsymmetric(properties.getProperty("keys.main", "RSA"));
        SYMMETRIC_ENCRYPTION = Encryptions.findSymmetric(properties.getProperty("keys.symmetric", "AES"));
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
