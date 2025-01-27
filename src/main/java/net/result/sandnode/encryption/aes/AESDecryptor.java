package net.result.sandnode.encryption.aes;

import net.result.sandnode.exception.DecryptionException;
import net.result.sandnode.exception.ImpossibleRuntimeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class AESDecryptor {
    private static final Logger LOGGER = LogManager.getLogger(AESDecryptor.class);

    public static String decrypt(byte[] data, @NotNull AESKeyStorage aesKeyStorage) throws DecryptionException {
        return new String(decryptBytes(data, aesKeyStorage));
    }

    public static byte[] decryptBytes(byte[] data, @NotNull AESKeyStorage aesKeyStorage) throws DecryptionException {
        SecretKey aesKey = aesKeyStorage.key();

        Cipher cipher;
        byte[] decrypted;

        byte[] iv = new byte[16];
        byte[] encryptedData = new byte[data.length - 16];
        System.arraycopy(data, 0, iv, 0, iv.length);
        System.arraycopy(data, 16, encryptedData, 0, encryptedData.length);

        try {
            cipher = Cipher.getInstance("AES/GCM/NoPadding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            LOGGER.error(e);
            throw new ImpossibleRuntimeException(e);
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

        return decrypted;
    }
}
