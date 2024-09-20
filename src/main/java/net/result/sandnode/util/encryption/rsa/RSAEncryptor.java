package net.result.sandnode.util.encryption.rsa;

import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.rsa.RSAEncryptionException;
import net.result.sandnode.util.encryption.asymmetric.rsa.RSAKeyStorage;
import net.result.sandnode.util.encryption.interfaces.IEncryptor;
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

import static java.nio.charset.StandardCharsets.US_ASCII;

public class RSAEncryptor implements IEncryptor {
    private static final Logger LOGGER = LogManager.getLogger(RSAEncryptor.class);

    private byte[] encrypt(@NotNull String data, @NotNull RSAKeyStorage rsaKeyStorage) throws RSAEncryptionException {
        return encryptBytes(data.getBytes(US_ASCII), rsaKeyStorage);
    }

    private byte[] encryptBytes(byte @NotNull [] data, @NotNull RSAKeyStorage rsaKeyStorage) throws RSAEncryptionException {
        final Cipher cipher;
        final byte[] encryptedBytes;

        try {
            cipher = Cipher.getInstance("RSA");
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
            throw new RSAEncryptionException(e);
        }

        return encryptedBytes;
    }

    @Override
    public byte[] encrypt(@NotNull String data, @NotNull IKeyStorage keyStorage) throws RSAEncryptionException, ReadingKeyException {
        if (keyStorage instanceof RSAKeyStorage rsaKeyStorage) return encrypt(data, rsaKeyStorage);
        else throw new ReadingKeyException("Key storage is not instance of RSAKeyStorage");
    }

    @Override
    public byte[] encryptBytes(byte @NotNull [] data, @NotNull IKeyStorage keyStorage) throws RSAEncryptionException, ReadingKeyException {
        if (keyStorage instanceof RSAKeyStorage rsaKeyStorage) return encryptBytes(data, rsaKeyStorage);
        else throw new ReadingKeyException("Key storage is not instance of RSAKeyStorage");
    }

}
