package net.result.sandnode.encryption.aes;

import net.result.sandnode.exceptions.DecryptionException;
import net.result.sandnode.encryption.interfaces.IDecryptor;
import net.result.sandnode.encryption.interfaces.IKeyStorage;
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
    private static final AESDecryptor INSTANCE = new AESDecryptor();

    public static AESDecryptor instance() {
        return INSTANCE;
    }

    public String decrypt(byte @NotNull [] data, @NotNull AESKeyStorage aesKeyStorage) throws DecryptionException {
        return new String(decryptBytes(data, aesKeyStorage));
    }

    public byte[] decryptBytes(byte @NotNull [] data, @NotNull AESKeyStorage aesKeyStorage) throws DecryptionException {
        SecretKey aesKey = aesKeyStorage.key();

        Cipher cipher;
        byte[] decrypted;

        byte[] iv = new byte[16];
        byte[] encryptedData = new byte[data.length - 16];
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
            throw new DecryptionException(e);
        }

        try {
            decrypted = cipher.doFinal(encryptedData);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            LOGGER.error("Encrypting error", e);
            throw new DecryptionException(e);
        }

        LOGGER.info("Data successfully decrypted with AES");
        return decrypted;
    }

    @Override
    public String decrypt(byte @NotNull [] data, @NotNull IKeyStorage keyStorage) throws DecryptionException {
        AESKeyStorage aesKeyStorage = (AESKeyStorage) keyStorage;
        return decrypt(data, aesKeyStorage);
    }

    @Override
    public byte[] decryptBytes(byte @NotNull [] data, @NotNull IKeyStorage keyStorage) throws DecryptionException {
        AESKeyStorage aesKeyStorage = (AESKeyStorage) keyStorage;
        return decryptBytes(data, aesKeyStorage);
    }

}
