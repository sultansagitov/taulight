package net.result.sandnode.util.encryption.asymmetric.rsa;

import net.result.sandnode.config.HubConfig;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.util.FileUtil;
import net.result.sandnode.util.encryption.asymmetric.interfaces.IAsymmetricKeySaver;
import net.result.sandnode.util.encryption.core.interfaces.IKeyStorage;
import net.result.sandnode.util.encryption.core.rsa.RSAKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class RSAKeySaver implements IAsymmetricKeySaver {
    private static final Logger LOGGER = LogManager.getLogger(RSAKeySaver.class);
    private static final RSAKeySaver INSTANCE = new RSAKeySaver();

    @Contract(pure = true)
    public static RSAKeySaver instance() {
        return INSTANCE;
    }

    private static boolean deleteFile(@NotNull Path filePath) {
        boolean result = false;
        File file = filePath.toFile();

        if (!file.exists()) {
            LOGGER.info("File \"{}\" does not exist, no need to delete.", filePath);
        } else {
            LOGGER.warn("RSA key file found in \"{}\", it will be deleted now.", filePath);
            if (file.delete()) {
                LOGGER.info("File \"{}\" successfully deleted.", filePath);
                result = true;
            } else {
                LOGGER.error("Failed to delete file \"{}\".", filePath);
            }
        }

        return result;
    }

    public void saveKeys(
            @NotNull RSAKeyStorage rsaKeyStorage,
            @NotNull Path publicKeyPath,
            @NotNull Path privateKeyPath
    ) throws IOException, ReadingKeyException {
        boolean isPublicFileDeleted = deleteFile(publicKeyPath);
        boolean isPrivateFileDeleted = deleteFile(privateKeyPath);

        String publicString = RSAPublicKeyConvertor.instance().toEncodedString(rsaKeyStorage);
        String privateString = RSAPrivateKeyConvertor.instance().toEncodedString(rsaKeyStorage);

        if (!isPublicFileDeleted || !isPrivateFileDeleted) {
            try (
                    FileWriter publicKeyWriter = new FileWriter(publicKeyPath.toString());
                    FileWriter privateKeyWriter = new FileWriter(privateKeyPath.toString())
            ) {
                LOGGER.info("Writing public and private keys to files.");
                publicKeyWriter.write(publicString);
                privateKeyWriter.write(privateString);

                LOGGER.info("Setting key file permissions.");
                FileUtil.makeOwnerOnlyRead(publicKeyPath);
                FileUtil.makeOwnerOnlyRead(privateKeyPath);
            } catch (IOException e) {
                LOGGER.error("Error writing keys to files", e);
                throw e;
            }
        }

    }

    @Override
    public void savePublicKey(
            @NotNull Path publicKeyPath,
            @NotNull IKeyStorage rsaKeyStorage
    ) throws IOException, ReadingKeyException {
        if (!Files.exists(publicKeyPath)) {
            try {
                Files.createFile(publicKeyPath);
                LOGGER.info("Public key file created at path: {}", publicKeyPath);
            } catch (IOException e) {
                LOGGER.error("Failed to create public key file", e);
                throw e;
            }
        } else {
            deleteFile(publicKeyPath);
        }

        String publicKeyString = RSAPublicKeyConvertor.instance().toEncodedString(rsaKeyStorage);

        try (FileWriter publicKeyWriter = new FileWriter(publicKeyPath.toString())) {
            LOGGER.info("Writing public key to file.");
            publicKeyWriter.write(publicKeyString);

            LOGGER.info("Setting public key file permissions.");
            FileUtil.makeOwnerOnlyRead(publicKeyPath);
        } catch (IOException e) {
            LOGGER.error("Error writing public key to file", e);
            throw e;
        }
    }


    @Override
    public void saveHubKeys(
            @NotNull HubConfig hubConfig,
            @NotNull IKeyStorage keyStorage
    ) throws IOException, ReadingKeyException {

        Path publicKeyPath = hubConfig.getPublicKeyPath();
        Path privateKeyPath = hubConfig.getPrivateKeyPath();

        if (keyStorage instanceof RSAKeyStorage) {
            RSAKeyStorage rsaKeyStorage = (RSAKeyStorage) keyStorage;
            saveKeys(rsaKeyStorage, publicKeyPath, privateKeyPath);
        } else {
            throw new ReadingKeyException("Key storage is not an instance of RSAKeyStorage");
        }
    }
}
