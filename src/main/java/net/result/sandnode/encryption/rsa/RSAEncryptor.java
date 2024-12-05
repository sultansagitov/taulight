package net.result.sandnode.encryption.rsa;

import net.result.sandnode.exceptions.EncryptionException;
import net.result.sandnode.encryption.interfaces.IEncryptor;
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

import static java.nio.charset.StandardCharsets.US_ASCII;

public class RSAEncryptor implements IEncryptor {
    private static final Logger LOGGER = LogManager.getLogger(RSAEncryptor.class);
    private static final RSAEncryptor INSTANCE = new RSAEncryptor();

    public static RSAEncryptor instance() {
        return INSTANCE;
    }

    private byte[] encrypt(@NotNull String data, @NotNull RSAKeyStorage rsaKeyStorage) throws EncryptionException {
        return encryptBytes(data.getBytes(US_ASCII), rsaKeyStorage);
    }

    private byte[] encryptBytes(byte @NotNull [] data, @NotNull RSAKeyStorage rsaKeyStorage)
            throws EncryptionException {
        Cipher cipher;
        byte[] encryptedBytes;

        try {
            cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            LOGGER.error("I hope you never see this error in your logs", e);
            throw new RuntimeException(e);
        }

        try {
            cipher.init(Cipher.ENCRYPT_MODE, rsaKeyStorage.getPublicKey());
            encryptedBytes = cipher.doFinal(data);
            LOGGER.info("Data successfully encrypted with RSA");
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            LOGGER.error("Invalid RSA key has been used", e);
            throw new EncryptionException(e);
        }

        return encryptedBytes;
    }

    @Override
    public byte[] encrypt(@NotNull String data, @NotNull IKeyStorage keyStorage) throws EncryptionException {
        RSAKeyStorage rsaKeyStorage = (RSAKeyStorage) keyStorage;
        return encrypt(data, rsaKeyStorage);
    }

    @Override
    public byte[] encryptBytes(byte @NotNull [] data, @NotNull IKeyStorage keyStorage) throws EncryptionException {
        RSAKeyStorage rsaKeyStorage = (RSAKeyStorage) keyStorage;
        return encryptBytes(data, rsaKeyStorage);
    }

}
