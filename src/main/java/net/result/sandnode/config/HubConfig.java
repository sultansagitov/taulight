package net.result.sandnode.config;

import net.result.sandnode.exceptions.ConfigurationException;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.CannotUseEncryption;
import net.result.sandnode.util.FileUtil;
import net.result.sandnode.util.encryption.Encryption;
import net.result.sandnode.util.encryption.asymmetric.Asymmetric;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class HubConfig implements NodeConfig {
    private static final Logger LOGGER = LogManager.getLogger(HubConfig.class);

    private final Encryption MAIN_ENCRYPTION;
    private final Encryption SYMMETRIC_ENCRYPTION;
    private final Path KEYS_DIR;
    private final Path PUBLIC_KEY_PATH;
    private final Path PRIVATE_KEY_PATH;

    public HubConfig() {
        this("hub.properties");
    }

    public HubConfig(@NotNull String fileName) {
        Properties properties = new Properties();

        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                throw new RuntimeException(String.format("Unable to find %s", fileName));
            }
            properties.load(input);
        } catch (IOException e) {
            throw new ConfigurationException(String.format("Failed to load configuration file: %s", fileName));
        }

        String defaultDirPath = "~/.openhelo/hub/";
        String defaultKeysDirPath = "keys/";

        Path CONF_DIR = FileUtil.resolveHomeInPath(Paths.get(properties.getProperty("hub.dir_path", defaultDirPath)));

        try {
            FileUtil.createDir(CONF_DIR);
        } catch (IOException e) {
            throw new RuntimeException("Error creating configuration directory", e);
        }

        MAIN_ENCRYPTION = Encryption.valueOf(properties.getProperty("keys.main", "RSA").toUpperCase());
        SYMMETRIC_ENCRYPTION = Encryption.valueOf(properties.getProperty("keys.symmetric", "AES").toUpperCase());
        KEYS_DIR = FileUtil.resolveHomeInPath(Paths.get(
                CONF_DIR.toString(),
                properties.getProperty("keys.dir_path", defaultKeysDirPath)
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
            try {
                FileUtil.createDir(KEYS_DIR);
            } catch (IOException e) {
                throw new RuntimeException(String.format("Error creating \"%s\"", KEYS_DIR), e);
            }
        }

        try {
            if (!PUBLIC_KEY_PATH.toFile().exists() && !PRIVATE_KEY_PATH.toFile().exists()) {
                Asymmetric.getKeySaver(MAIN_ENCRYPTION)
                        .saveHubKeys(this, MAIN_ENCRYPTION.generator().generateKeyStorage());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error managing RSA keys", e);
        } catch (ReadingKeyException | CannotUseEncryption e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull Encryption getMainEncryption() {
        return MAIN_ENCRYPTION;
    }

    @Override
    public @NotNull Encryption getSymmetricKeyEncryption() {
        return SYMMETRIC_ENCRYPTION;
    }

    public Path getKeysDir() {
        return KEYS_DIR;
    }

    public Path getPublicKeyPath() {
        return PUBLIC_KEY_PATH;
    }

    public Path getPrivateKeyPath() {
        return PRIVATE_KEY_PATH;
    }

}
