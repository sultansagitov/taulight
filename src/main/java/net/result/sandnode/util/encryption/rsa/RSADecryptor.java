package net.result.sandnode.util.encryption.rsa;

import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.rsa.RSADecryptionException;
import net.result.sandnode.util.encryption.asymmetric.rsa.RSAKeyStorage;
import net.result.sandnode.util.encryption.interfaces.IDecryptor;
import net.result.sandnode.util.encryption.interfaces.IKeyStorage;
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
    private static final RSADecryptor instance = new RSADecryptor();

    public static RSADecryptor getInstance() {
        return instance;
    }

    @Override
    public String decrypt(byte @NotNull [] data, @NotNull IKeyStorage keyStore) throws RSADecryptionException, ReadingKeyException {
        byte[] decryptedBytes = decryptBytes(data, keyStore);
        return new String(decryptedBytes);
    }

    public byte[] decryptBytes(
            byte @NotNull [] data,
            @NotNull RSAKeyStorage keyStorage
    ) throws RSADecryptionException {
        Cipher cipher;
        byte[] decryptedBytes;

        try {
            cipher = Cipher.getInstance("RSA");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            LOGGER.error("I hope you never see this error in your logs", e);
            throw new RuntimeException(e);
        }

        try {
            cipher.init(Cipher.DECRYPT_MODE, keyStorage.getPrivateKey());
        } catch (InvalidKeyException e) {
            LOGGER.error("Invalid RSA key has been used", e);
            throw new RSADecryptionException(e);
        }

        try {
            decryptedBytes = cipher.doFinal(data);
            LOGGER.info("Data successfully decrypted with RSA");
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            LOGGER.error("Error when decrypt", e);
            throw new RSADecryptionException(e);
        }

        return decryptedBytes;
    }

    @Override
    public byte[] decryptBytes(
            byte @NotNull [] data,
            @NotNull IKeyStorage keyStorage
    ) throws RSADecryptionException, ReadingKeyException {
        if (keyStorage instanceof RSAKeyStorage rsaKeyStorage) {
            return decryptBytes(data, rsaKeyStorage);
        } else {
            throw new ReadingKeyException("Key storage is not instance of RSAKeyStorage");
        }
    }

}
