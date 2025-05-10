package net.result.main.config;

import net.result.sandnode.config.ServerConfig;
import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.KeyStorageRegistry;
import net.result.sandnode.encryption.interfaces.AsymmetricConvertor;
import net.result.sandnode.encryption.interfaces.AsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.exception.ConfigurationException;
import net.result.sandnode.exception.FSException;
import net.result.sandnode.exception.ImpossibleRuntimeException;
import net.result.sandnode.exception.crypto.*;
import net.result.sandnode.security.PasswordHasher;
import net.result.sandnode.security.PasswordHashers;
import net.result.sandnode.util.Endpoint;
import net.result.sandnode.util.FileUtil;
import net.result.sandnode.db.Database;
import net.result.sandnode.group.GroupManager;
import net.result.sandnode.security.Tokenizer;
import net.result.taulight.db.TauJPADatabase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Properties;

public class ServerPropertiesConfig implements ServerConfig {
    private static final Logger LOGGER = LogManager.getLogger(ServerPropertiesConfig.class);
    private final Endpoint endpoint;
    private final Path PUBLIC_KEY_PATH;
    private final Path PRIVATE_KEY_PATH;
    private final AsymmetricEncryption MAIN_ENCRYPTION;
    private GroupManager groupManager;
    private Database database;
    private Tokenizer tokenizer;
    private final PasswordHasher HASHER;

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

        if (endpoint != null) {
            this.endpoint = endpoint;
        } else {
            String host = properties.getProperty("server.host", defaultHost);
            int port = properties.containsKey("server.port")
                    ? Integer.parseInt(properties.getProperty("server.port"))
                    : defaultPort;
            this.endpoint = new Endpoint(host, port);
        }


        Path KEYS_DIR = FileUtil.resolveHome(Path.of(properties.getProperty("server.keys.dir_path")));

        String publicKeyProperty = properties.getProperty("server.keys.pubkey_path");
        String privateKeyProperty = properties.getProperty("server.keys.privkey_path");
        PUBLIC_KEY_PATH = FileUtil.resolveHome(KEYS_DIR.resolve(publicKeyProperty));
        PRIVATE_KEY_PATH = FileUtil.resolveHome(KEYS_DIR.resolve(privateKeyProperty));

        MAIN_ENCRYPTION = EncryptionManager.find(properties.getProperty("server.keys.main")).asymmetric();
        String hasherName = properties.getProperty("hub.hasher");
        HASHER = Arrays.stream(PasswordHashers.values())
                .filter(h -> h.name().equalsIgnoreCase(hasherName))
                .findFirst()
                .orElse(PasswordHashers.values()[0]);

        if (!Files.exists(KEYS_DIR)) {
            LOGGER.info("KEYS_DIR does not exist, creating it: \"{}\"", KEYS_DIR);
            FileUtil.createDir(KEYS_DIR);
        }

        try {
            setDatabase(new TauJPADatabase());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Endpoint endpoint() {
        return endpoint;
    }

    @Override
    public @NotNull AsymmetricEncryption mainEncryption() {
        return MAIN_ENCRYPTION;
    }

    public void setGroupManager(GroupManager groupManager) {
        this.groupManager = groupManager;
    }

    @Override
    public GroupManager groupManager() {
        return groupManager;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    @Override
    public Database database() {
        return database;
    }

    public void setTokenizer(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    @Override
    public Tokenizer tokenizer() {
        return tokenizer;
    }

    @Override
    public void saveKey(AsymmetricKeyStorage keyStorage) throws SavingKeyException {
        boolean isPublicFileDeleted = FileUtil.deleteFile(PUBLIC_KEY_PATH);
        boolean isPrivateFileDeleted = FileUtil.deleteFile(PRIVATE_KEY_PATH);

        String publicString;
        String privateString;
        try {
            publicString = keyStorage.encodedPublicKey();
            privateString = keyStorage.encodedPrivateKey();
        } catch (CannotUseEncryption e) {
            throw new ImpossibleRuntimeException(e);
        }

        if (!isPublicFileDeleted || !isPrivateFileDeleted) {
            try (
                    FileWriter publicKeyWriter = new FileWriter(PUBLIC_KEY_PATH.toString());
                    FileWriter privateKeyWriter = new FileWriter(PRIVATE_KEY_PATH.toString())
            ) {
                LOGGER.info("Writing public and private keys to files.");
                publicKeyWriter.write(publicString);
                privateKeyWriter.write(privateString);

                LOGGER.info("Setting key file permissions.");
                FileUtil.makeOwnerOnlyRead(PUBLIC_KEY_PATH);
                FileUtil.makeOwnerOnlyRead(PRIVATE_KEY_PATH);
            } catch (IOException | FSException e) {
                throw new SavingKeyException("Error writing keys to files", e);
            }
        }
    }

    @Override
    public KeyStorageRegistry readKey(AsymmetricEncryption mainEncryption)
            throws CreatingKeyException, ReadingKeyException {
        AsymmetricKeyStorage publicKeyStorage;
        AsymmetricKeyStorage privateKeyStorage;
        try {
            LOGGER.info("Reading public key in \"{}\"", PUBLIC_KEY_PATH);
            AsymmetricConvertor publicKeyConvertor = mainEncryption.publicKeyConvertor();
            String publicKeyString = FileUtil.readString(PUBLIC_KEY_PATH);
            publicKeyStorage = publicKeyConvertor.toKeyStorage(publicKeyString);

            LOGGER.info("Reading private key in \"{}\"", PRIVATE_KEY_PATH);
            AsymmetricConvertor privateKeyConvertor = mainEncryption.privateKeyConvertor();
            String string = FileUtil.readString(PRIVATE_KEY_PATH);
            privateKeyStorage = privateKeyConvertor.toKeyStorage(string);
        } catch (FSException e) {
            throw new ReadingKeyException("Error reading keys from files", e);
        }
        AsymmetricKeyStorage keyStorage = mainEncryption.merge(publicKeyStorage, privateKeyStorage);
        return new KeyStorageRegistry(keyStorage);
    }

    @Override
    public PasswordHasher hasher() {
        return HASHER;
    }
}
