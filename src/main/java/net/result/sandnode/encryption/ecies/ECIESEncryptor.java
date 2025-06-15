package net.result.sandnode.encryption.ecies;

import net.result.sandnode.encryption.AsymmetricEncryptions;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.exception.crypto.CryptoException;
import net.result.sandnode.exception.error.EncryptionException;
import net.result.sandnode.exception.ImpossibleRuntimeException;
import net.result.sandnode.exception.crypto.WrongKeyException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;

public class ECIESEncryptor {
    private static final Logger LOGGER = LogManager.getLogger(ECIESEncryptor.class);

    public static byte[] encrypt(@NotNull String data, @NotNull KeyStorage keyStorage)
            throws EncryptionException, CryptoException {
        return encryptBytes(data.getBytes(StandardCharsets.UTF_8), keyStorage);
    }

    public static byte[] encryptBytes(byte @NotNull [] data, @NotNull KeyStorage keyStorage)
            throws EncryptionException, CryptoException {
        ECIESKeyStorage eciesKeyStorage = (ECIESKeyStorage) keyStorage.expect(AsymmetricEncryptions.ECIES);
        Cipher cipher;
        try {
            cipher = Cipher.getInstance("ECIES", "BC");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException e) {
            LOGGER.error(e);
            throw new ImpossibleRuntimeException(e);
        }

        PublicKey publicKey = eciesKeyStorage.publicKey();
        try {
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        } catch (InvalidKeyException e) {
            throw new WrongKeyException("The provided key is invalid for encryption", e);
        }

        byte[] result;
        try {
            result = cipher.doFinal(data);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new EncryptionException("An error occurred during encryption", e);
        }

        return result;
    }
}
