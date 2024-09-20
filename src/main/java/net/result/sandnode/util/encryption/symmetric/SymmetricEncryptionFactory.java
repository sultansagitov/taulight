package net.result.sandnode.util.encryption.symmetric;

import net.result.sandnode.util.encryption.GlobalKeyStorage;
import net.result.sandnode.util.encryption.aes.AESDecryptor;
import net.result.sandnode.util.encryption.aes.AESGenerator;
import net.result.sandnode.util.encryption.interfaces.IDecryptor;
import net.result.sandnode.util.encryption.symmetric.aes.AESKeyStorage;
import net.result.sandnode.util.encryption.symmetric.interfaces.ISymmetricGenerator;
import net.result.sandnode.util.encryption.symmetric.interfaces.SymmetricKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.security.NoSuchAlgorithmException;

public class SymmetricEncryptionFactory {
    private static final Logger LOGGER = LogManager.getLogger(SymmetricEncryptionFactory.class);

    public static @NotNull ISymmetricGenerator getGenerator(@NotNull String algorithm) throws NoSuchAlgorithmException {
        final String ALG = algorithm.toUpperCase();

        return switch (ALG) {
            case "AES" -> new AESGenerator();
            default -> throw new NoSuchAlgorithmException("Can't find algorithm \"%s\"".formatted(algorithm));
        };
    }

    public static @NotNull IDecryptor getDecryptor(@NotNull String algorithm) throws NoSuchAlgorithmException {
        final String ALG = algorithm.toUpperCase();

        return switch (ALG) {
            case "AES" -> new AESDecryptor();
            default -> throw new NoSuchAlgorithmException("Can't find algorithm \"" + algorithm + "\"");
        };
    }

    public static @NotNull SymmetricKeyStorage getKeyStorage(@NotNull GlobalKeyStorage globalKeyStorage, @NotNull String algorithm) throws NoSuchAlgorithmException {
        final String ALG = algorithm.toUpperCase();

        return switch (ALG) {
            case "AES" -> globalKeyStorage.getAESKeyStorage();
            default -> throw new NoSuchAlgorithmException("Can't find algorithm \"" + algorithm + "\"");
        };
    }

    public static void setKeyStorage(
            @NotNull GlobalKeyStorage globalKeyStorage,
            @NotNull String algorithm,
            @NotNull SymmetricKeyStorage keyStorage
    ) throws NoSuchAlgorithmException {
        final String ALG = algorithm.toUpperCase();

        switch (ALG) {
            case "AES":
                globalKeyStorage.setAESKeyStorage((AESKeyStorage) keyStorage);
                break;
            default:
                throw new NoSuchAlgorithmException("Can't find algorithm \"" + algorithm + "\"");
        }
    }
}
