package net.result.sandnode.encryption.rsa;

import net.result.sandnode.encryption.AsymmetricEncryptions;
import net.result.sandnode.exception.*;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

public class RSADecryptor {
    private static final Logger LOGGER = LogManager.getLogger(RSADecryptor.class);

    public static String decrypt(byte @NotNull [] data, @NotNull KeyStorage keyStorage)
            throws DecryptionException, WrongKeyException, CannotUseEncryption, PrivateKeyNotFoundException {
        return new String(decryptBytes(data, keyStorage));
    }

    public static byte[] decryptBytes(
            byte @NotNull [] data,
            @NotNull KeyStorage keyStorage
    ) throws DecryptionException, WrongKeyException, CannotUseEncryption, PrivateKeyNotFoundException {
        RSAKeyStorage rsaKeyStorage = (RSAKeyStorage) keyStorage.expect(AsymmetricEncryptions.RSA);
        Cipher cipher;

        try {
            cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            LOGGER.error(e);
            throw new ImpossibleRuntimeException(e);
        }

        PrivateKey privateKey = rsaKeyStorage.privateKey();

        if (privateKey == null) {
            throw new PrivateKeyNotFoundException("Private key is missing or not initialized.");
        }

        try {
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
        } catch (InvalidKeyException e) {
            LOGGER.error(e);
            throw new WrongKeyException("The provided key is invalid for encryption", e);
        }

        byte[] result;
        try {
            result = cipher.doFinal(data);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new DecryptionException(e);
        }

        return result;
    }
}
