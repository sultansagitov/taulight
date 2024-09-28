package net.result.sandnode.util.encryption.asymmetric;

import net.result.sandnode.exceptions.encryption.CannotUseEncryption;
import net.result.sandnode.util.encryption.Encryption;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import net.result.sandnode.util.encryption.asymmetric.interfaces.IAsymmetricConvertor;
import net.result.sandnode.util.encryption.asymmetric.rsa.RSAKeyStorage;
import net.result.sandnode.util.encryption.asymmetric.rsa.RSAPrivateKeyConvertor;
import net.result.sandnode.util.encryption.asymmetric.rsa.RSAPublicKeyConvertor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.NoSuchAlgorithmException;

import static net.result.sandnode.util.encryption.Encryption.RSA;

public class AsymmetricEncryptionFactory {

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

    public static @NotNull AsymmetricKeyStorage getKeyStorage(
            @NotNull GlobalKeyStorage globalKeyStorage,
            @NotNull Encryption encryption
    ) throws NoSuchAlgorithmException {
        return switch (encryption) {
            case RSA -> globalKeyStorage.getRSAKeyStorage();
            default -> throw new NoSuchAlgorithmException("Can't use algorithm \"" + encryption + "\"");
        };
    }

    public static @Nullable Encryption setKeyStorage(
            @NotNull GlobalKeyStorage globalKeyStorage,
            @NotNull AsymmetricKeyStorage publicKey
    ) {
        if (publicKey instanceof RSAKeyStorage rsaPublicKey) {
            globalKeyStorage.setRSAKeyStorage(rsaPublicKey);
            return RSA;
        }
        return null;
    }
}
