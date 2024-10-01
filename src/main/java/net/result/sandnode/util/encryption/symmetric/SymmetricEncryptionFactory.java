package net.result.sandnode.util.encryption.symmetric;

import net.result.sandnode.util.encryption.Encryption;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import net.result.sandnode.util.encryption.aes.AESDecryptor;
import net.result.sandnode.util.encryption.aes.AESGenerator;
import net.result.sandnode.util.encryption.interfaces.IDecryptor;
import net.result.sandnode.util.encryption.symmetric.aes.AESKeyConvertor;
import net.result.sandnode.util.encryption.symmetric.aes.AESKeyStorage;
import net.result.sandnode.util.encryption.symmetric.interfaces.ISymmetricGenerator;
import net.result.sandnode.util.encryption.symmetric.interfaces.ISymmetricKeyConvertor;
import net.result.sandnode.util.encryption.symmetric.interfaces.SymmetricKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.NoSuchAlgorithmException;

public class SymmetricEncryptionFactory {

    public static @NotNull ISymmetricGenerator getGenerator(@NotNull Encryption encryption) throws NoSuchAlgorithmException {
        return switch (encryption) {
            case AES -> AESGenerator.getInstance();
            default -> throw new NoSuchAlgorithmException("Can't find algorithm \"%s\"".formatted(encryption));
        };
    }

    public static @NotNull IDecryptor getDecryptor(@NotNull Encryption encryption) throws NoSuchAlgorithmException {
        return switch (encryption) {
            case AES -> AESDecryptor.getInstance();
            default -> throw new NoSuchAlgorithmException("Can't find algorithm \"" + encryption + "\"");
        };
    }

    public static @Nullable SymmetricKeyStorage getKeyStorage(@NotNull GlobalKeyStorage globalKeyStorage, @NotNull Encryption encryption) throws NoSuchAlgorithmException {
        return switch (encryption) {
            case AES -> globalKeyStorage.getAESKeyStorage();
            default -> throw new NoSuchAlgorithmException("Can't find algorithm \"" + encryption + "\"");
        };
    }

    public static void setKeyStorage(
            @NotNull GlobalKeyStorage globalKeyStorage,
            @NotNull Encryption encryption,
            @NotNull SymmetricKeyStorage keyStorage
    ) throws NoSuchAlgorithmException {
        switch (encryption) {
            case AES -> globalKeyStorage.setAESKeyStorage((AESKeyStorage) keyStorage);
            default -> throw new NoSuchAlgorithmException("Can't find algorithm \"" + encryption + "\"");
        }
    }

    public static ISymmetricKeyConvertor getKeyConvertor(@NotNull Encryption encryption) throws NoSuchAlgorithmException {
        return switch (encryption) {
            case AES -> AESKeyConvertor.getInstance();
            default -> throw new NoSuchAlgorithmException("Can't find algorithm \"" + encryption + "\"");
        };
    }
}
