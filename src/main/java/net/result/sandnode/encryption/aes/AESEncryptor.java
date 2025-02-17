package net.result.sandnode.encryption.aes;

import net.result.sandnode.exception.EncryptionException;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.exception.ImpossibleRuntimeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class AESEncryptor {
    private static final Logger LOGGER = LogManager.getLogger(AESEncryptor.class);

    public static byte[] encrypt(@NotNull String data, @NotNull KeyStorage keyStorage) throws EncryptionException {
        byte[] bytes = data.trim().getBytes(StandardCharsets.US_ASCII);
        return encryptBytes(bytes, keyStorage);
    }

    public static byte[] encryptBytes(byte @NotNull [] data, @NotNull KeyStorage keyStorage)
            throws EncryptionException {
        int IV_LENGTH = 16;
        byte[] iv = new byte[IV_LENGTH];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        SecretKey aesKey = ((AESKeyStorage) keyStorage).key();
        Cipher cipher;
        byte[] encrypted;

        try {
            cipher = Cipher.getInstance("AES/GCM/NoPadding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            LOGGER.error(e);
            throw new ImpossibleRuntimeException(e);
        }

        try {
            cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            LOGGER.error("Error when initializing cypher", e);
            throw new EncryptionException(e);
        }

        try {
            encrypted = cipher.doFinal(data);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            LOGGER.error("Encrypting error", e);
            throw new EncryptionException(e);
        }

        byte[] result = new byte[IV_LENGTH + encrypted.length];
        System.arraycopy(iv, 0, result, 0, IV_LENGTH);
        System.arraycopy(encrypted, 0, result, IV_LENGTH, encrypted.length);
        return result;
    }

}
