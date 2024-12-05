package net.result.sandnode.encryption.rsa;

import net.result.sandnode.config.IServerConfig;
import net.result.sandnode.exceptions.CreatingKeyException;
import net.result.sandnode.encryption.interfaces.IAsymmetricKeyReader;
import net.result.sandnode.exceptions.FSException;
import net.result.sandnode.util.FileUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class RSAKeyReader implements IAsymmetricKeyReader {
    private static final Logger LOGGER = LogManager.getLogger(RSAKeyReader.class);
    private static final RSAKeyReader INSTANCE = new RSAKeyReader();

    public static RSAKeyReader instance() {
        return INSTANCE;
    }

    @Override
    public RSAKeyStorage readKeys(@NotNull IServerConfig serverConfig) throws CreatingKeyException, FSException {
        Path publicKeyPath = serverConfig.publicKeyPath();
        Path privateKeyPath = serverConfig.privateKeyPath();

        LOGGER.info("Reading public key in \"{}\"", publicKeyPath);
        RSAPublicKeyConvertor publicKeyConvertor = RSAPublicKeyConvertor.instance();
        String publicKeyString = FileUtil.readString(publicKeyPath);
        RSAKeyStorage publicKeyStorage = publicKeyConvertor.toKeyStorage(publicKeyString);

        LOGGER.info("Reading private key in \"{}\"", privateKeyPath);
        RSAPrivateKeyConvertor privateKeyConvertor = RSAPrivateKeyConvertor.instance();
        String string = FileUtil.readString(privateKeyPath);
        RSAKeyStorage privateKeyStorage = privateKeyConvertor.toKeyStorage(string);

        return new RSAKeyStorage(publicKeyStorage.getPublicKey(), privateKeyStorage.getPrivateKey());
    }
}
