package net.result.sandnode.util.encryption.asymmetric.rsa;

import net.result.sandnode.config.ServerConfigSingleton;
import net.result.sandnode.exceptions.CreatingKeyException;
import net.result.sandnode.util.encryption.asymmetric.interfaces.IAsymmetricConvertor;
import net.result.sandnode.util.encryption.asymmetric.interfaces.IKeyReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;

public class RSAKeyReader implements IKeyReader {
    private static final Logger LOGGER = LogManager.getLogger(RSAKeyReader.class);
    private static final RSAKeyReader instance = new RSAKeyReader();

    public static RSAKeyReader getInstance() {
        return instance;
    }

    @Override
    public RSAKeyStorage readKeys() throws IOException, CreatingKeyException {
        RSAKeyStorage publicKeyStore = getKeyStore(
                ServerConfigSingleton.getRSAPublicKeyPath().toString(),
                RSAPublicKeyConvertor.getInstance()
        );
        RSAKeyStorage privateKeyStore = getKeyStore(
                ServerConfigSingleton.getRSAPrivateKeyPath().toString(),
                RSAPrivateKeyConvertor.getInstance()
        );
        KeyPair keyPair = new KeyPair(publicKeyStore.getPublicKey(), privateKeyStore.getPrivateKey());
        return new RSAKeyStorage(keyPair);
    }

    private static @NotNull String getKeyPEM(@NotNull String keyPath) throws IOException {
        try {
            return new String(Files.readAllBytes(Paths.get(keyPath)));
        } catch (IOException e) {
            LOGGER.error("Can't read file \"{}\"", keyPath, e);
            throw e;
        }
    }

    private static @NotNull RSAKeyStorage getKeyStore(
            @NotNull String keyPath,
            @NotNull IAsymmetricConvertor keyConvertor
    ) throws CreatingKeyException, IOException {
        String PEM = getKeyPEM(keyPath);
        try {
            return (RSAKeyStorage) keyConvertor.toKeyStorage(PEM);
        } catch (CreatingKeyException e) {
            LOGGER.error("Invalid key in \"{}\" file", keyPath, e);
            throw e;
        }
    }
}
