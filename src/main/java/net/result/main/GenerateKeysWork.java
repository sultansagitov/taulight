package net.result.main;

import net.result.sandnode.config.IServerConfig;
import net.result.main.config.ServerPropertiesConfig;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.exception.*;
import net.result.sandnode.encryption.interfaces.IAsymmetricEncryption;
import net.result.sandnode.util.FileUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class GenerateKeysWork implements IWork {
    private static final Logger LOGGER = LogManager.getLogger(GenerateKeysWork.class);

    @Override
    public void run() throws NoSuchEncryptionException, ConfigurationException, FSException, EncryptionTypeException {
        IServerConfig serverConfig = new ServerPropertiesConfig();
        IAsymmetricEncryption mainEncryption = serverConfig.mainEncryption();
        AsymmetricKeyStorage keyStorage = mainEncryption.generate();

        Path publicKeyPath = serverConfig.publicKeyPath();
        Path privateKeyPath = serverConfig.privateKeyPath();

        boolean isPublicFileDeleted = FileUtil.deleteFile(publicKeyPath);
        boolean isPrivateFileDeleted = FileUtil.deleteFile(privateKeyPath);

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
                throw new FSException("Error writing keys to files", e);
            }
        }
    }
}
