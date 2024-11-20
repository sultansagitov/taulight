package net.result.sandnode.config;

import net.result.sandnode.exceptions.ConfigurationException;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.CannotUseEncryption;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.util.FileUtil;
import net.result.sandnode.util.encryption.Encryptions;
import net.result.sandnode.util.encryption.interfaces.IAsymmetricEncryption;
import net.result.sandnode.util.encryption.interfaces.ISymmetricEncryption;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class HubPropertiesConfig implements IHubConfig {
    private static final Logger LOGGER = LogManager.getLogger(HubPropertiesConfig.class);

    private final IAsymmetricEncryption MAIN_ENCRYPTION;
    private final ISymmetricEncryption SYMMETRIC_ENCRYPTION;
    private final Path KEYS_DIR;
    private final Path PUBLIC_KEY_PATH;
    private final Path PRIVATE_KEY_PATH;

    public HubPropertiesConfig() throws NoSuchEncryptionException, ConfigurationException, CannotUseEncryption,
            IOException, ReadingKeyException {
        this("hub.properties");
    }

    public HubPropertiesConfig(@NotNull String fileName) throws ConfigurationException, NoSuchEncryptionException,
            CannotUseEncryption, IOException, ReadingKeyException {
        Properties properties = new Properties();

        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                throw new RuntimeException(String.format("Unable to find %s", fileName));
            }
            properties.load(input);
        } catch (IOException e) {
            throw new ConfigurationException(String.format("Failed to load configuration file: %s", fileName));
        }

        Path CONF_DIR = FileUtil.resolveHomeInPath(Paths.get(properties.getProperty("hub.dir_path")));

        FileUtil.createDir(CONF_DIR);


        MAIN_ENCRYPTION = Encryptions.findAsymmetric(properties.getProperty("keys.main", "RSA"));
        SYMMETRIC_ENCRYPTION = Encryptions.findSymmetric(properties.getProperty("keys.symmetric", "AES"));
        KEYS_DIR = FileUtil.resolveHomeInPath(Paths.get(
                CONF_DIR.toString(),
                properties.getProperty("keys.dir_path")
        ));

        PUBLIC_KEY_PATH = FileUtil.resolveHomeInPath(Paths.get(
                KEYS_DIR.toString(),
                properties.getProperty("keys.public_key_path")
        ));
        PRIVATE_KEY_PATH = FileUtil.resolveHomeInPath(Paths.get(
                KEYS_DIR.toString(),
                properties.getProperty("keys.private_key_path")
        ));

        if (!Files.exists(KEYS_DIR)) {
            LOGGER.info("KEYS_DIR does not exist, creating it: \"{}\"", KEYS_DIR);
            FileUtil.createDir(KEYS_DIR);
        }

        if (!PUBLIC_KEY_PATH.toFile().exists() && !PRIVATE_KEY_PATH.toFile().exists()) {
            MAIN_ENCRYPTION.keySaver()
                    .saveHubKeys(this, MAIN_ENCRYPTION.generator().generate());
        }
    }

    @Override
    public @NotNull IAsymmetricEncryption getMainEncryption() {
        return MAIN_ENCRYPTION;
    }

    @Override
    public @NotNull ISymmetricEncryption getSymmetricKeyEncryption() {
        return SYMMETRIC_ENCRYPTION;
    }

    @Override
    public Path getKeysDir() {
        return KEYS_DIR;
    }

    @Override
    public Path getPublicKeyPath() {
        return PUBLIC_KEY_PATH;
    }

    @Override
    public Path getPrivateKeyPath() {
        return PRIVATE_KEY_PATH;
    }

}
