package net.result.sandnode.encryption.rsa;

import net.result.sandnode.exceptions.DecryptionException;
import net.result.sandnode.encryption.interfaces.IDecryptor;
import net.result.sandnode.encryption.interfaces.IKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class RSADecryptor implements IDecryptor {
    private static final Logger LOGGER = LogManager.getLogger(RSADecryptor.class);
    private static final RSADecryptor INSTANCE = new RSADecryptor();

    public static RSADecryptor instance() {
        return INSTANCE;
    }

    @Override
    public String decrypt(byte @NotNull [] data, @NotNull IKeyStorage keyStorage) throws DecryptionException {
        byte[] decryptedBytes = decryptBytes(data, keyStorage);
        return new String(decryptedBytes);
    }

    public byte[] decryptBytes(byte @NotNull [] data, @NotNull RSAKeyStorage keyStorage) throws DecryptionException {
        Cipher cipher;
        byte[] decryptedBytes;

        try {
            cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            LOGGER.error("I hope you never see this error in your logs", e);
            throw new RuntimeException(e);
        }

        try {
            cipher.init(Cipher.DECRYPT_MODE, keyStorage.getPrivateKey());
        } catch (InvalidKeyException e) {
            LOGGER.error("Invalid RSA key has been used", e);
            throw new DecryptionException(e);
        }

        try {
            decryptedBytes = cipher.doFinal(data);
            LOGGER.info("Data successfully decrypted with RSA");
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            LOGGER.error("Error when decrypt", e);
            throw new DecryptionException(e);
        }

        return decryptedBytes;
    }

    @Override
    public byte[] decryptBytes(byte @NotNull [] data, @NotNull IKeyStorage keyStorage) throws DecryptionException {
        RSAKeyStorage rsaKeyStorage = (RSAKeyStorage) keyStorage;
        return decryptBytes(data, rsaKeyStorage);
    }

}
