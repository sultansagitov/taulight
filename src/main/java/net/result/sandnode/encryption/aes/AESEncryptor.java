package net.result.sandnode.encryption.aes;

import net.result.sandnode.exceptions.EncryptionException;
import net.result.sandnode.encryption.interfaces.IEncryptor;
import net.result.sandnode.encryption.interfaces.IKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import static java.nio.charset.StandardCharsets.US_ASCII;

public class AESEncryptor implements IEncryptor {
    private static final Logger LOGGER = LogManager.getLogger(AESEncryptor.class);
    private static final AESEncryptor INSTANCE = new AESEncryptor();

    public static AESEncryptor instance() {
        return INSTANCE;
    }

    public byte[] encrypt(@NotNull String data, @NotNull AESKeyStorage keyStorage) throws EncryptionException {
        byte[] bytes = data.trim().getBytes(US_ASCII);
        return encryptBytes(bytes, keyStorage);
    }

    public byte[] encryptBytes(byte @NotNull [] data, @NotNull AESKeyStorage keyStorage) throws EncryptionException {
        int IV_LENGTH = 16;
        byte[] iv = new byte[IV_LENGTH];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        SecretKey aesKey = keyStorage.key();
        Cipher cipher;
        byte[] encrypted;

        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            LOGGER.error("I hope you never see this error in your logs", e);
            throw new RuntimeException(e);
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
        LOGGER.info("Data successfully encrypted with AES");
        return result;
    }

    @Override
    public byte[] encrypt(@NotNull String data, @NotNull IKeyStorage keyStorage) throws EncryptionException {
        AESKeyStorage aesKeyStorage = (AESKeyStorage) keyStorage;
        return encrypt(data, aesKeyStorage);
    }

    @Override
    public byte[] encryptBytes(byte @NotNull [] data, @NotNull IKeyStorage keyStorage) throws EncryptionException {
        AESKeyStorage aesKeyStorage = (AESKeyStorage) keyStorage;
        return encryptBytes(data, aesKeyStorage);
    }

}
