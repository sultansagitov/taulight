package net.result.sandnode.encryption.rsa;

import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.exception.CannotUseEncryption;
import net.result.sandnode.exception.EncryptionException;
import net.result.sandnode.exception.WrongKeyException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static net.result.sandnode.encryption.AsymmetricEncryptions.RSA;

public class RSAEncryptor {
    private static final Logger LOGGER = LogManager.getLogger(RSAEncryptor.class);

    public static byte[] encrypt(@NotNull String data, @NotNull KeyStorage rsaKeyStorage)
            throws EncryptionException, CannotUseEncryption {
        return encryptBytes(data.getBytes(US_ASCII), rsaKeyStorage);
    }

    public static byte[] encryptBytes(byte @NotNull [] data, @NotNull KeyStorage keyStorage)
            throws EncryptionException, CannotUseEncryption {
        Cipher cipher;
        byte[] encryptedBytes;

        RSAKeyStorage rsaKeyStorage = (RSAKeyStorage) keyStorage.expect(RSA);

        try {
            cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new WrongKeyException("The provided key is invalid for encryption", e);
        }

        try {
            cipher.init(Cipher.ENCRYPT_MODE, rsaKeyStorage.publicKey());
            encryptedBytes = cipher.doFinal(data);
            LOGGER.info("Data successfully encrypted with RSA");
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            LOGGER.error("Invalid RSA key has been used", e);
            throw new EncryptionException(e);
        }

        return encryptedBytes;
    }
}
