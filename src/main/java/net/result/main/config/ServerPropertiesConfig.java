package net.result.main.config;

import net.result.sandnode.config.IServerConfig;
import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.interfaces.IAsymmetricEncryption;
import net.result.sandnode.exception.*;
import net.result.sandnode.util.Endpoint;
import net.result.sandnode.util.FileUtil;
import net.result.sandnode.db.IDatabase;
import net.result.sandnode.group.GroupManager;
import net.result.sandnode.tokens.ITokenizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

public class ServerPropertiesConfig implements IServerConfig {
    private static final Logger LOGGER = LogManager.getLogger(ServerPropertiesConfig.class);
    private final Endpoint endpoint;
    private final Path PUBLIC_KEY_PATH;
    private final Path PRIVATE_KEY_PATH;
    private final IAsymmetricEncryption MAIN_ENCRYPTION;
    private GroupManager groupManager;
    private IDatabase database;
    private ITokenizer tokenizer;

    public ServerPropertiesConfig()
            throws ConfigurationException, FSException, NoSuchEncryptionException, EncryptionTypeException {
        this("taulight.properties", null);
    }

    public ServerPropertiesConfig(String fileName, @Nullable Endpoint endpoint)
            throws ConfigurationException, FSException, NoSuchEncryptionException, EncryptionTypeException {
        Properties properties = new Properties();

        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (input == null)
                throw new ImpossibleRuntimeException("Unable to find %s".formatted(fileName));
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

        Path KEYS_DIR = FileUtil.resolveHome(Path.of(properties.getProperty("server.keys.dir_path")));

        String publicKeyProperty = properties.getProperty("server.keys.pubkey_path");
        String privateKeyProperty = properties.getProperty("server.keys.privkey_path");
        PUBLIC_KEY_PATH = FileUtil.resolveHome(KEYS_DIR.resolve(publicKeyProperty));
        PRIVATE_KEY_PATH = FileUtil.resolveHome(KEYS_DIR.resolve(privateKeyProperty));

        MAIN_ENCRYPTION = EncryptionManager.find(properties.getProperty("server.keys.main")).asymmetric();

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

    @Override
    public @NotNull IAsymmetricEncryption mainEncryption() {
        return MAIN_ENCRYPTION;
    }

    public void setGroupManager(GroupManager groupManager) {
        this.groupManager = groupManager;
    }

    @Override
    public GroupManager groupManager() {
        return groupManager;
    }

    public void setDatabase(IDatabase database) {
        this.database = database;
    }

    @Override
    public IDatabase database() {
        return database;
    }

    public void setTokenizer(ITokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    @Override
    public ITokenizer tokenizer() {
        return tokenizer;
    }
}
