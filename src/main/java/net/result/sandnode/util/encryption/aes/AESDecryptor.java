package net.result.sandnode.util.encryption.aes;

import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.aes.AESDecryptionException;
import net.result.sandnode.util.encryption.interfaces.IDecryptor;
import net.result.sandnode.util.encryption.interfaces.IKeyStorage;
import net.result.sandnode.util.encryption.symmetric.aes.AESKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class AESDecryptor implements IDecryptor {
    private static final Logger LOGGER = LogManager.getLogger(AESDecryptor.class);
    private static final AESDecryptor instance = new AESDecryptor();

    private AESDecryptor() {}

    public static AESDecryptor getInstance() {
        return instance;
    }

    public String decrypt(
            byte @NotNull [] data,
            @NotNull AESKeyStorage aesKeyStorage
    ) throws AESDecryptionException {
        return new String(decryptBytes(data, aesKeyStorage));
    }

    public byte[] decryptBytes(
            byte @NotNull [] data,
            @NotNull AESKeyStorage aesKeyStorage
    ) throws AESDecryptionException {
        final SecretKey aesKey = aesKeyStorage.getKey();

        final Cipher cipher;
        final byte[] decrypted;

        final byte[] iv = new byte[16];
        final byte[] encryptedData = new byte[data.length - 16];
        System.arraycopy(data, 0, iv, 0, iv.length);
        System.arraycopy(data, 16, encryptedData, 0, encryptedData.length);

        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            LOGGER.error("I hope you never see this error in your logs", e);
            throw new RuntimeException(e);
        }

        try {
            cipher.init(Cipher.DECRYPT_MODE, aesKey, new IvParameterSpec(iv));
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            LOGGER.error("Error when initializing cypher", e);
            throw new AESDecryptionException(e);
        }

        try {
            decrypted = cipher.doFinal(encryptedData);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            LOGGER.error("Encrypting error", e);
            throw new AESDecryptionException(e);
        }

        LOGGER.info("Data successfully decrypted with AES");
        return decrypted;
    }

    @Override
    public String decrypt(
            byte @NotNull [] data,
            @NotNull IKeyStorage keyStorage
    ) throws AESDecryptionException, ReadingKeyException {
        if (keyStorage instanceof AESKeyStorage aesKeyStorage) return decrypt(data, aesKeyStorage);
        else throw new ReadingKeyException("Key storage is not instance of AESKeyStorage");
    }

    @Override
    public byte[] decryptBytes(
            byte @NotNull [] data,
            @NotNull IKeyStorage keyStorage
    ) throws AESDecryptionException, ReadingKeyException {
        if (keyStorage instanceof AESKeyStorage aesKeyStorage) return decryptBytes(data, aesKeyStorage);
        else throw new ReadingKeyException("Key storage is not instance of AESKeyStorage");
    }

}
