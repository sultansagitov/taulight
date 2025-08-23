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
import net.result.sandnode.util.Container;
import net.result.sandnode.util.Address;
import net.result.sandnode.util.FileUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ServerPropertiesConfig implements ServerConfig {
    private static final Logger LOGGER = LogManager.getLogger(ServerPropertiesConfig.class);
    private final Container container;
    private final Address address;
    private final Path PUBLIC_KEY_PATH;
    private final Path PRIVATE_KEY_PATH;
    private final AsymmetricEncryption MAIN_ENCRYPTION;

    public ServerPropertiesConfig(Container container) {
        this(container, "taulight.properties", null);
    }

    public ServerPropertiesConfig(Container container, String fileName, @Nullable Address address) {
        this.container = container;

        Properties properties = new Properties();

        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                throw new ImpossibleRuntimeException("Unable to find %s".formatted(fileName));
            }
            properties.load(input);
        } catch (IOException e) {
            throw new ConfigurationException("Failed to load configuration file", fileName);
        }

        String defaultHost = "127.0.0.1";
        int defaultPort = 52525;

        if (address != null) {
            this.address = address;
        } else {
            String host = properties.getProperty("server.host", defaultHost);
            int port = properties.containsKey("server.port")
                    ? Integer.parseInt(properties.getProperty("server.port"))
                    : defaultPort;
            this.address = new Address(host, port);
        }


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
    public Container container() {
        return container;
    }

    @Override
    public Address address() {
        return address;
    }

    @Override
    public @NotNull AsymmetricEncryption mainEncryption() {
        return MAIN_ENCRYPTION;
    }

    @Override
    public void saveKey(AsymmetricKeyStorage keyStorage) {
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
                if (FileUtil.isPosixSupported()) {
                    FileUtil.makeOwnerOnlyRead(PUBLIC_KEY_PATH);
                    FileUtil.makeOwnerOnlyRead(PRIVATE_KEY_PATH);
                } else {
                    LOGGER.warn("POSIX unsupported here");
                }
            } catch (IOException | FSException e) {
                throw new SavingKeyException("Error writing keys to files", e);
        }
        }
    }

    @Override
    public KeyStorageRegistry readKey(AsymmetricEncryption mainEncryption) {
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
}
