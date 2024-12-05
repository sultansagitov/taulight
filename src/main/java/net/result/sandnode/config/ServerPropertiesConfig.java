package net.result.sandnode.config;

import net.result.sandnode.exceptions.ConfigurationException;
import net.result.sandnode.exceptions.FSException;
import net.result.sandnode.util.Endpoint;
import net.result.sandnode.util.FileUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;

public class ServerPropertiesConfig implements IServerConfig {
    private static final Logger LOGGER = LogManager.getLogger(ServerPropertiesConfig.class);
    private final Endpoint endpoint;
    private final @NotNull Path PUBLIC_KEY_PATH;
    private final @NotNull Path PRIVATE_KEY_PATH;

    public ServerPropertiesConfig() throws ConfigurationException, FSException {
        this("server.properties", null);
    }

    public ServerPropertiesConfig(@NotNull String fileName, @Nullable Endpoint endpoint) throws ConfigurationException,
            FSException {
        Properties properties = new Properties();

        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (input == null)
                throw new RuntimeException(String.format("Unable to find %s", fileName));
            properties.load(input);
        } catch (IOException e) {
            throw new ConfigurationException("Failed to load configuration file", fileName);
        }

        String defaultHost = "127.0.0.1";
        int defaultPort = 52525;

        this.endpoint = Objects.requireNonNullElseGet(endpoint, () -> new Endpoint(
                properties.getProperty("server.host", defaultHost),
                Integer.parseInt(properties.getProperty("server.port", "" + defaultPort))
        ));

        String CONF_DIR = properties.getProperty("server.dir_path");
        Path KEYS_DIR = FileUtil.resolveHomeInPath(Paths.get(CONF_DIR, properties.getProperty("keys.dir_path")));

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
    }

    @Override
    public Endpoint endpoint() {
        return endpoint;
    }

    @Override
    public Path publicKeyPath() {
        return PUBLIC_KEY_PATH;
    }

    @Override
    public Path privateKeyPath() {
        return PRIVATE_KEY_PATH;
    }

}
