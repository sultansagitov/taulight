package net.result.sandnode.config;

import net.result.sandnode.exceptions.ConfigurationException;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.util.encryption.asymmetric.rsa.RSAKeySaver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static net.result.sandnode.util.encryption.Encryption.RSA;

public class HubConfig {
    private static final Logger LOGGER = LogManager.getLogger(HubConfig.class);

    private final Path CONF_DIR;
    private final Path KEYS_DIR;
    private final boolean USES_RSA;
    private Path RSA_PUBLIC_KEY_PATH;
    private Path RSA_PRIVATE_KEY_PATH;

    public HubConfig() {
        this("hub.properties");
    }

    public HubConfig(@NotNull String fileName) {
        Properties properties = new Properties();

        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                throw new RuntimeException("Unable to find %s".formatted(fileName));
            }
            properties.load(input);
        } catch (IOException e) {
            throw new ConfigurationException("Failed to load configuration file: %s".formatted(fileName));
        }

        String defaultDirPath = "~/.openhelo/server/";
        boolean defaultUseRsa = false;
        String defaultKeysDirPath = "keys/";

        CONF_DIR = PathUtil.resolveHomeInPath(Paths.get(properties.getProperty("server.dir_path", defaultDirPath)));

        try {
            PathUtil.createDir(CONF_DIR);
        } catch (IOException e) {
            throw new RuntimeException("Error creating configuration directory", e);
        }

        USES_RSA = Boolean.parseBoolean(properties.getProperty("keys.rsa.use", String.valueOf(defaultUseRsa)));
        KEYS_DIR = PathUtil.resolveHomeInPath(Paths.get(CONF_DIR.toString(), properties.getProperty("keys.dir_path", defaultKeysDirPath)));

        if (USES_RSA) {
            RSA_PUBLIC_KEY_PATH = PathUtil.resolveHomeInPath(Paths.get(
                    KEYS_DIR.toString(),
                    properties.getProperty("keys.rsa.public_key_path")
            ));
            RSA_PRIVATE_KEY_PATH = PathUtil.resolveHomeInPath(Paths.get(
                    KEYS_DIR.toString(),
                    properties.getProperty("keys.rsa.private_key_path")
            ));

            if (!Files.exists(KEYS_DIR)) {
                LOGGER.info("KEYS_DIR does not exist, creating it: \"{}\"", KEYS_DIR);
                try {
                    PathUtil.createDir(KEYS_DIR);
                } catch (IOException e) {
                    throw new RuntimeException("Error creating \"%s\"".formatted(KEYS_DIR), e);
                }
            }

            try {
                if (!RSA_PUBLIC_KEY_PATH.toFile().exists() && !RSA_PRIVATE_KEY_PATH.toFile().exists()) {
                    RSAKeySaver.getInstance().saveKeys(this, RSA.generator().generateKeyStorage());
                }
            } catch (IOException e) {
                throw new RuntimeException("Error managing RSA keys", e);
            } catch (ReadingKeyException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Path getConfDir() {
        return CONF_DIR;
    }

    public Path getKeysDir() {
        return KEYS_DIR;
    }

    public Path getRSAPublicKeyPath() {
        return RSA_PUBLIC_KEY_PATH;
    }

    public Path getRSAPrivateKeyPath() {
        return RSA_PRIVATE_KEY_PATH;
    }

    public boolean useRSA() {
        return USES_RSA;
    }
}
