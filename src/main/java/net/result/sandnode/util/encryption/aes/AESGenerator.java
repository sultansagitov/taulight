package net.result.sandnode.util.encryption.aes;

import net.result.sandnode.util.encryption.symmetric.aes.AESKeyStorage;
import net.result.sandnode.util.encryption.symmetric.interfaces.ISymmetricGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;

public class AESGenerator implements ISymmetricGenerator {
    private static final Logger LOGGER = LogManager.getLogger(AESGenerator.class);
    private static final AESGenerator instance = new AESGenerator();

    public static AESGenerator getInstance() {
        return instance;
    }

    @Override
    public AESKeyStorage generateKeyStorage() {
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
        return AESKeyStorage.getInstance().setKey(secretKey);
    }
}
