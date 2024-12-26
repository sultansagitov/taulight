package net.result.sandnode.encryption.aes;

import net.result.sandnode.exceptions.ImpossibleRuntimeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;

public class AESGenerator {
    private static final Logger LOGGER = LogManager.getLogger(AESGenerator.class);

    public static @NotNull AESKeyStorage generate() {
        KeyGenerator keyGenerator;
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error(e);
            throw new ImpossibleRuntimeException(e);
        }
        keyGenerator.init(256);

        SecretKey secretKey = keyGenerator.generateKey();
        LOGGER.info("AES key successfully generated");
        return new AESKeyStorage(secretKey);
    }
}
