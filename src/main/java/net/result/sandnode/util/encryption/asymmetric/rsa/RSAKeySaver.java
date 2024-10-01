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
    private static final RSAKeySaver instance = new RSAKeySaver();

    public static final Path SERVER_PUBLIC_KEY_PATH = ServerConfigSingleton.getRSAPublicKeyPath();
    public static final Path SERVER_PRIVATE_KEY_PATH = ServerConfigSingleton.getRSAPrivateKeyPath();

    private RSAKeySaver() {
    }

    public static RSAKeySaver getInstance() {
        return instance;
    }

    public void saveKeys(@NotNull RSAKeyStorage keyStorage) throws IOException, ReadingKeyException {
        boolean publicFileEx = deleteFile(SERVER_PUBLIC_KEY_PATH);
        boolean privateFileEx = deleteFile(SERVER_PRIVATE_KEY_PATH);
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
        String publicKeyPEM = RSAPublicKeyConvertor.getInstance().toPEM(keyStore);
        String privateKeyPEM = RSAPrivateKeyConvertor.getInstance().toPEM(keyStore);
        if (publicFileEx && privateFileEx) return;

        try (
                FileWriter publicKeyWriter = new FileWriter(SERVER_PUBLIC_KEY_PATH.toString());
                FileWriter privateKeyWriter = new FileWriter(SERVER_PRIVATE_KEY_PATH.toString())) {
            publicKeyWriter.write(publicKeyPEM);
            privateKeyWriter.write(privateKeyPEM);

            KeyManagerUtil.setKeyFilePermissions(SERVER_PUBLIC_KEY_PATH);
            KeyManagerUtil.setKeyFilePermissions(SERVER_PRIVATE_KEY_PATH);
        }
    }

    private static boolean deleteFile(@NotNull Path path) {
        File file = new File(path.toString());

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

