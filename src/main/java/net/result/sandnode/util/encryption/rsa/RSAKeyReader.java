package net.result.sandnode.util.encryption.rsa;

import net.result.sandnode.config.IHubConfig;
import net.result.sandnode.exceptions.CreatingKeyException;
import net.result.sandnode.util.encryption.interfaces.IAsymmetricKeyReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class RSAKeyReader implements IAsymmetricKeyReader {
    private static final Logger LOGGER = LogManager.getLogger(RSAKeyReader.class);
    private static final RSAKeyReader INSTANCE = new RSAKeyReader();

    public static RSAKeyReader instance() {
        return INSTANCE;
    }

    @Override
    public RSAKeyStorage readKeys(@NotNull IHubConfig hubConfig) throws IOException, CreatingKeyException {
        Path publicKeyPath = hubConfig.getPublicKeyPath();
        Path privateKeyPath = hubConfig.getPrivateKeyPath();

        LOGGER.info("Reading public key in \"{}\"", publicKeyPath);
        RSAPublicKeyConvertor publicKeyConvertor = RSAPublicKeyConvertor.instance();
        String publicKeyString = Files.readString(publicKeyPath);
        RSAKeyStorage publicKeyStorage = publicKeyConvertor.toKeyStorage(publicKeyString);

        LOGGER.info("Reading private key in \"{}\"", privateKeyPath);
        RSAPrivateKeyConvertor privateKeyConvertor = RSAPrivateKeyConvertor.instance();
        String string = Files.readString(privateKeyPath);
        RSAKeyStorage privateKeyStorage = privateKeyConvertor.toKeyStorage(string);

        return new RSAKeyStorage(publicKeyStorage.getPublicKey(), privateKeyStorage.getPrivateKey());
    }
}
