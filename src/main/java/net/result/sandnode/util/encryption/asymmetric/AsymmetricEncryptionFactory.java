package net.result.sandnode.util.encryption.asymmetric;

import net.result.sandnode.util.encryption.Encryption;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import net.result.sandnode.util.encryption.asymmetric.interfaces.IAsymmetricConvertor;
import net.result.sandnode.util.encryption.asymmetric.rsa.RSAPrivateKeyConvertor;
import net.result.sandnode.util.encryption.asymmetric.rsa.RSAPublicKeyConvertor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.security.NoSuchAlgorithmException;

public class AsymmetricEncryptionFactory {
    private static final Logger LOGGER = LogManager.getLogger(AsymmetricEncryptionFactory.class);

    public static @NotNull IAsymmetricConvertor getPublicConvertor(@NotNull Encryption encryption) throws NoSuchAlgorithmException {
        return switch (encryption) {
            case RSA -> new RSAPublicKeyConvertor();
            default -> throw new NoSuchAlgorithmException("Can't find algorithm \"" + encryption + "\"");
        };
    }

    public static @NotNull IAsymmetricConvertor getPrivateConvertor(@NotNull Encryption encryption) throws NoSuchAlgorithmException {
        return switch (encryption) {
            case RSA -> new RSAPrivateKeyConvertor();
            default -> throw new NoSuchAlgorithmException("Can't find algorithm \"" + encryption + "\"");
        };
    }

    public static @NotNull AsymmetricKeyStorage getKeyStorage(
            final @NotNull GlobalKeyStorage globalKeyStorage,
            final @NotNull Encryption encryption
    ) throws NoSuchAlgorithmException {
        return switch (encryption) {
            case RSA -> globalKeyStorage.getRSAKeyStorage();
            default -> throw new NoSuchAlgorithmException("Can't find algorithm \"" + encryption + "\"");
        };
    }

}
