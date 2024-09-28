package net.result.sandnode.util.encryption.aes;

import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.aes.AESEncryptionException;
import net.result.sandnode.util.encryption.interfaces.IEncryptor;
import net.result.sandnode.util.encryption.interfaces.IKeyStorage;
import net.result.sandnode.util.encryption.symmetric.aes.AESKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import static java.nio.charset.StandardCharsets.US_ASCII;

public class AESEncryptor implements IEncryptor {
    private static final Logger LOGGER = LogManager.getLogger(AESEncryptor.class);
    private static final AESEncryptor instance = new AESEncryptor();

    private AESEncryptor() {
    }

    public static AESEncryptor getInstance() {
        return instance;
    }

    public byte[] encrypt(@NotNull String data, @Nullable AESKeyStorage keyStore) throws AESEncryptionException {
        final byte[] bytes = data.trim().getBytes(US_ASCII);
        return encryptBytes(bytes, keyStore);
    }

    public byte[] encryptBytes(byte @NotNull [] data, @Nullable AESKeyStorage keyStore) throws AESEncryptionException {
        final int IV_LENGTH = 16;
        final byte[] iv = new byte[IV_LENGTH];
        final SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);
        final IvParameterSpec ivSpec = new IvParameterSpec(iv);

        final SecretKey aesKey = keyStore.getKey();
        final Cipher cipher;
        final byte[] encrypted;

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
            throw new AESEncryptionException(e);
        }

        try {
            encrypted = cipher.doFinal(data);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            LOGGER.error("Encrypting error", e);
            throw new AESEncryptionException(e);
        }

        final byte[] result = new byte[IV_LENGTH + encrypted.length];
        System.arraycopy(iv, 0, result, 0, IV_LENGTH);
        System.arraycopy(encrypted, 0, result, IV_LENGTH, encrypted.length);
        LOGGER.info("Data successfully encrypted with AES");
        return result;
    }

    @Override
    public byte[] encrypt(@NotNull String data, @Nullable IKeyStorage keyStorage) throws AESEncryptionException, ReadingKeyException {
        if (keyStorage instanceof AESKeyStorage aesKeyStorage) return encrypt(data, aesKeyStorage);
        else throw new ReadingKeyException("Key storage is not instance of AESKeyStorage");
    }

    @Override
    public byte[] encryptBytes(byte @NotNull [] data, @Nullable IKeyStorage keyStorage) throws AESEncryptionException, ReadingKeyException {
        if (keyStorage instanceof AESKeyStorage aesKeyStorage) return encryptBytes(data, aesKeyStorage);
        else throw new ReadingKeyException("Key storage is not instance of AESKeyStorage");
    }

}
