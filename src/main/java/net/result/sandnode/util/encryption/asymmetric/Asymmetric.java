package net.result.sandnode.util.encryption.asymmetric;

import net.result.sandnode.exceptions.encryption.CannotUseEncryption;
import net.result.sandnode.util.encryption.Encryption;
import net.result.sandnode.util.encryption.asymmetric.interfaces.IAsymmetricConvertor;
import net.result.sandnode.util.encryption.asymmetric.interfaces.IAsymmetricKeyReader;
import net.result.sandnode.util.encryption.asymmetric.interfaces.IAsymmetricKeySaver;
import net.result.sandnode.util.encryption.asymmetric.rsa.RSAKeyReader;
import net.result.sandnode.util.encryption.asymmetric.rsa.RSAKeySaver;
import net.result.sandnode.util.encryption.asymmetric.rsa.RSAPrivateKeyConvertor;
import net.result.sandnode.util.encryption.asymmetric.rsa.RSAPublicKeyConvertor;
import org.jetbrains.annotations.NotNull;

public class Asymmetric {

    public static @NotNull IAsymmetricConvertor getPublicConvertor(@NotNull Encryption encryption) throws CannotUseEncryption {
        return switch (encryption) {
            case RSA -> RSAPublicKeyConvertor.instance();
            default -> throw new CannotUseEncryption(String.format("Can't use algorithm \"%s\"", encryption));
        };
    }

    public static @NotNull IAsymmetricConvertor getPrivateConvertor(@NotNull Encryption encryption) throws CannotUseEncryption {
        return switch (encryption) {
            case RSA -> RSAPrivateKeyConvertor.instance();
            default -> throw new CannotUseEncryption(String.format("Can't use algorithm \"%s\"", encryption));
        };
    }

    public static @NotNull IAsymmetricKeySaver getKeySaver(@NotNull Encryption encryption) throws CannotUseEncryption {
        return switch (encryption) {
            case RSA -> RSAKeySaver.instance();
            default -> throw new CannotUseEncryption(String.format("Can't use algorithm \"%s\"", encryption));
        };
    }

    public static @NotNull IAsymmetricKeyReader getKeyReader(@NotNull Encryption encryption) throws CannotUseEncryption {
        return switch (encryption) {
            case RSA -> RSAKeyReader.instance();
            default -> throw new CannotUseEncryption(String.format("Can't use algorithm \"%s\"", encryption));
        };
    }

}
