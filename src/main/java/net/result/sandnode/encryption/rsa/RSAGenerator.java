package net.result.sandnode.encryption.rsa;

import net.result.sandnode.exceptions.ImpossibleRuntimeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class RSAGenerator {
    private static final Logger LOGGER = LogManager.getLogger(RSAGenerator.class);

    public static @NotNull RSAKeyStorage generate() {
        KeyPairGenerator keyGen;

        try {
            keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error(e);
            throw new ImpossibleRuntimeException(e);
        }

        KeyPair keyPair = keyGen.generateKeyPair();
        LOGGER.info("RSA key pair successfully generated");

        return new RSAKeyStorage(keyPair);
    }

}
