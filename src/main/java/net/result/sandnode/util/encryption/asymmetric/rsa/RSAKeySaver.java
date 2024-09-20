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

public class RSAKeySaver implements IKeySaver {
    private static final Logger LOGGER = LogManager.getLogger(RSAKeySaver.class);

    public static final String PUBLIC_KEY_PATH = ServerConfigSingleton.getRSAPublicKeyPath();
    public static final String PRIVATE_KEY_PATH = ServerConfigSingleton.getRSAPrivateKeyPath();

    public void saveKeys(@NotNull RSAKeyStorage keyStorage) throws IOException, ReadingKeyException {
        boolean publicFileEx = deleteFile(PUBLIC_KEY_PATH);
        boolean privateFileEx = deleteFile(PRIVATE_KEY_PATH);
        writeKeys(keyStorage, publicFileEx, privateFileEx);
    }

    @Override
    public void saveKeys(@NotNull IKeyStorage keyStorage) throws IOException, ReadingKeyException {
        if (keyStorage instanceof RSAKeyStorage rsaKeyStorage) saveKeys(rsaKeyStorage);
        else throw new ReadingKeyException("Key storage is not instance of RSAKeyStorage");
    }

    private static void writeKeys(
            @NotNull IKeyStorage keyStore,
            boolean publicFileEx,
            boolean privateFileEx
    ) throws IOException, ReadingKeyException {
        final String publicKeyPEM = new RSAPublicKeyConvertor().toPEM(keyStore);
        final String privateKeyPEM = new RSAPrivateKeyConvertor().toPEM(keyStore);
        if (publicFileEx && privateFileEx) return;

        try (
                FileWriter publicKeyWriter = new FileWriter(PUBLIC_KEY_PATH);
                FileWriter privateKeyWriter = new FileWriter(PRIVATE_KEY_PATH)) {
            publicKeyWriter.write(publicKeyPEM);
            privateKeyWriter.write(privateKeyPEM);

            KeyManagerUtil.setKeyFilePermissions(PUBLIC_KEY_PATH);
            KeyManagerUtil.setKeyFilePermissions(PRIVATE_KEY_PATH);
        }
    }

    private static boolean deleteFile(@NotNull String path) {
        final File file = new File(path);

        if (!file.exists()) return false;

        LOGGER.warn("RSA key file found in \"{}\", it will delete now", path);

        if (file.delete()) {
            LOGGER.info("File \"{}\" deleted", path);
            return false;
        }

        LOGGER.error("Can't delete file \"{}\"", path);
        return true;
    }

}

