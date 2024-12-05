package net.result.sandnode.encryption.aes;

import net.result.sandnode.encryption.interfaces.ISymmetricGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;

public class AESGenerator implements ISymmetricGenerator {
    private static final Logger LOGGER = LogManager.getLogger(AESGenerator.class);
    private static final AESGenerator INSTANCE = new AESGenerator();

    public static AESGenerator instance() {
        return INSTANCE;
    }

    @Override
    public @NotNull AESKeyStorage generate() {
        KeyGenerator keyGenerator;
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("I hope you never see this error in your logs", e);
            throw new RuntimeException(e);
        }
        keyGenerator.init(256);

        SecretKey secretKey = keyGenerator.generateKey();
        LOGGER.info("AES key successfully generated");
        return new AESKeyStorage(secretKey);
    }
}
