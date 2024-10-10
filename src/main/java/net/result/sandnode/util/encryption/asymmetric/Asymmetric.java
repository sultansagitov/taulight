package net.result.sandnode.util.encryption.asymmetric;

import net.result.sandnode.exceptions.encryption.CannotUseEncryption;
import net.result.sandnode.util.encryption.Encryption;
import net.result.sandnode.util.encryption.asymmetric.interfaces.IAsymmetricConvertor;
import net.result.sandnode.util.encryption.asymmetric.rsa.RSAPrivateKeyConvertor;
import net.result.sandnode.util.encryption.asymmetric.rsa.RSAPublicKeyConvertor;
import org.jetbrains.annotations.NotNull;

import java.security.NoSuchAlgorithmException;

public class Asymmetric {

    public static @NotNull IAsymmetricConvertor getPublicConvertor(@NotNull Encryption encryption) throws CannotUseEncryption {
        return switch (encryption) {
            case RSA -> RSAPublicKeyConvertor.getInstance();
            default -> throw new CannotUseEncryption("Can't use algorithm \"" + encryption + "\"");
        };
    }

    public static @NotNull IAsymmetricConvertor getPrivateConvertor(@NotNull Encryption encryption) throws NoSuchAlgorithmException {
        return switch (encryption) {
            case RSA -> RSAPrivateKeyConvertor.getInstance();
            default -> throw new NoSuchAlgorithmException("Can't use algorithm \"" + encryption + "\"");
        };
    }

}
