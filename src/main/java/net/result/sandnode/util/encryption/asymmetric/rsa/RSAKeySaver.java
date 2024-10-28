package net.result.sandnode.util.encryption.asymmetric.rsa;

import net.result.sandnode.config.ServerConfigSingleton;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.util.encryption.KeyManagerUtil;
import net.result.sandnode.util.encryption.asymmetric.interfaces.IKeySaver;
import net.result.sandnode.util.encryption.interfaces.IKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class RSAKeySaver implements IKeySaver {
    private static final Logger LOGGER = LogManager.getLogger(RSAKeySaver.class);
    private static final RSAKeySaver INSTANCE = new RSAKeySaver();

    public static RSAKeySaver getInstance() {
        return INSTANCE;
    }

    public void saveKeys(@NotNull RSAKeyStorage rsaKeyStorage, Path publicKeyPath, Path privateKeyPath) throws IOException, ReadingKeyException {
        boolean isPublicFileDeleted = deleteFile(publicKeyPath);
        boolean isPrivateFileDeleted = deleteFile(privateKeyPath);
        writeKeysToFile(rsaKeyStorage, publicKeyPath, privateKeyPath, isPublicFileDeleted, isPrivateFileDeleted);
    }

    @Override
    public void saveKeys(@NotNull IKeyStorage keyStorage) throws IOException, ReadingKeyException {
        Path publicKeyPath = ServerConfigSingleton.getRSAPublicKeyPath();
        Path privateKeyPath = ServerConfigSingleton.getRSAPrivateKeyPath();
        if (keyStorage instanceof RSAKeyStorage rsaKeyStorage) {
            saveKeys(rsaKeyStorage, publicKeyPath, privateKeyPath);
        } else {
            throw new ReadingKeyException("Key storage is not an instance of RSAKeyStorage");
        }
    }

    private static void writeKeysToFile(
            @NotNull IKeyStorage keyStorage,
            @NotNull Path publicKeyPath,
            @NotNull Path privateKeyPath,
            boolean isPublicFileDeleted,
            boolean isPrivateFileDeleted
    ) throws IOException, ReadingKeyException {
        String publicKeyPEM = RSAPublicKeyConvertor.getInstance().toPEM(keyStorage);
        String privateKeyPEM = RSAPrivateKeyConvertor.getInstance().toPEM(keyStorage);

        if (isPublicFileDeleted && isPrivateFileDeleted) {
            LOGGER.info("RSA key files already deleted. Skipping write operation.");
            return;
        }

        try (
                FileWriter publicKeyWriter = new FileWriter(publicKeyPath.toString());
                FileWriter privateKeyWriter = new FileWriter(privateKeyPath.toString())
        ) {
            LOGGER.info("Writing public and private keys to files.");
            publicKeyWriter.write(publicKeyPEM);
            privateKeyWriter.write(privateKeyPEM);

            LOGGER.info("Setting key file permissions.");
            KeyManagerUtil.setKeyFilePermissions(publicKeyPath);
            KeyManagerUtil.setKeyFilePermissions(privateKeyPath);
        } catch (IOException e) {
            LOGGER.error("Error writing keys to files", e);
            throw e;
        }
    }

    private static boolean deleteFile(@NotNull Path filePath) {
        File file = new File(filePath.toString());

        if (!file.exists()) {
            LOGGER.info("File \"{}\" does not exist, no need to delete.", filePath);
            return false;
        }

        LOGGER.warn("RSA key file found in \"{}\", it will be deleted now.", filePath);

        if (file.delete()) {
            LOGGER.info("File \"{}\" successfully deleted.", filePath);
            return true;
        } else {
            LOGGER.error("Failed to delete file \"{}\".", filePath);
            return false;
        }
    }
}
