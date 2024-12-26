package net.result.sandnode.encryption.ecies;

import net.result.sandnode.encryption.interfaces.IKeyStorage;
import net.result.sandnode.exceptions.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;

import static net.result.sandnode.encryption.AsymmetricEncryption.ECIES;

public class ECIESDecryptor {
    private static final Logger LOGGER = LogManager.getLogger(ECIESDecryptor.class);

    public static String decrypt(byte @NotNull [] data, @NotNull IKeyStorage keyStorage) throws DecryptionException,
            WrongKeyException, CannotUseEncryption, PrivateKeyNotFoundException {
        return new String(decryptBytes(data, keyStorage));
    }

    public static byte[] decryptBytes(byte @NotNull [] data, @NotNull IKeyStorage keyStorage)
            throws DecryptionException, WrongKeyException, CannotUseEncryption, PrivateKeyNotFoundException {
        ECIESKeyStorage eciesKeyStorage = (ECIESKeyStorage) keyStorage.expect(ECIES);
        Cipher cipher;
        try {
            cipher = Cipher.getInstance("ECIES", "BC");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException e) {
            LOGGER.error(e);
            throw new ImpossibleRuntimeException(e);
        }

        PrivateKey privateKey = eciesKeyStorage.privateKey();

        if (privateKey == null) {
            throw new PrivateKeyNotFoundException("Private key is missing or not initialized.");
        }

        try {
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
        } catch (InvalidKeyException e) {
            throw new WrongKeyException("The provided key is invalid for encryption", e);
        }

        byte[] result;
        try {
            result = cipher.doFinal(data);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new DecryptionException("An error occurred during encryption", e);
        }
        LOGGER.info("Data successfully decrypted with ECIES");
        return result;
    }
}
