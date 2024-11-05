package net.result.sandnode.util.encryption.asymmetric.rsa;

import net.result.sandnode.config.HubConfig;
import net.result.sandnode.exceptions.CreatingKeyException;
import net.result.sandnode.util.encryption.asymmetric.interfaces.IAsymmetricConvertor;
import net.result.sandnode.util.encryption.asymmetric.interfaces.IAsymmetricKeyReader;
import net.result.sandnode.util.encryption.core.rsa.RSAKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;

public class RSAKeyReader implements IAsymmetricKeyReader {
    private static final Logger LOGGER = LogManager.getLogger(RSAKeyReader.class);
    private static final RSAKeyReader INSTANCE = new RSAKeyReader();

    public static RSAKeyReader instance() {
        return INSTANCE;
    }

    private static @NotNull String getEncodedString(@NotNull String keyPath) throws IOException {
        try {
            return new String(Files.readAllBytes(Paths.get(keyPath)));
        } catch (IOException e) {
            LOGGER.error("Can't read file \"{}\"", keyPath, e);
            throw e;
        }
    }

    private static @NotNull RSAKeyStorage getKeyStorage(
            @NotNull String keyPath,
            @NotNull IAsymmetricConvertor keyConvertor
    ) throws CreatingKeyException, IOException {
        String string = getEncodedString(keyPath);
        try {
            return (RSAKeyStorage) keyConvertor.toKeyStorage(string);
        } catch (CreatingKeyException e) {
            LOGGER.error("Invalid key in \"{}\" file", keyPath, e);
            throw e;
        }
    }

    @Override
    public RSAKeyStorage readKeys(@NotNull HubConfig hubConfig) throws IOException, CreatingKeyException {
        RSAKeyStorage publicKeyStorage = getKeyStorage(
                hubConfig.getPublicKeyPath().toString(),
                RSAPublicKeyConvertor.instance()
        );
        RSAKeyStorage privateKeyStorage = getKeyStorage(
                hubConfig.getPrivateKeyPath().toString(),
                RSAPrivateKeyConvertor.instance()
        );
        KeyPair keyPair = new KeyPair(publicKeyStorage.getPublicKey(), privateKeyStorage.getPrivateKey());
        return new RSAKeyStorage(keyPair);
    }
}
